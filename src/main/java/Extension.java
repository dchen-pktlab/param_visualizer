import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.proxy.Proxy;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;

public class Extension implements BurpExtension {
    private RequestParser parser = new RequestParser();
    private RequestTable tableModel; 
    private MontoyaApi montoyaApi;
    private JTextField targetUrlField;

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;
        montoyaApi.extension().setName("Parameter Collector");

        this.tableModel = new RequestTable(parser.mapped_requests);

        // RequestHandler handler = new RequestHandler(parser, refreshTable);
        // montoyaApi.proxy().registerRequestHandler(handler);

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Parameter Collector", SwingConstants.CENTER);
        topPanel.add(title);


        JPanel targetPanel = new JPanel(new BorderLayout(5, 0));
        targetPanel.add(new JLabel("Target URL:"), BorderLayout.WEST);
        this.targetUrlField = new JTextField();
        int targetFieldWidth = Toolkit.getDefaultToolkit().getScreenSize().width / 2;
        targetUrlField.setPreferredSize(new Dimension(targetFieldWidth, targetUrlField.getPreferredSize().height));
        targetPanel.add(targetUrlField, BorderLayout.CENTER);

        JButton exportButton = new JButton("Collect URL Params");
        targetPanel.add(exportButton, BorderLayout.EAST);

        targetPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, targetPanel.getPreferredSize().height));
        topPanel.add(targetPanel);

        panel.add(topPanel, BorderLayout.NORTH);

        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(Object.class, new RequestTableRenderer());
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(180);
            } else {
                column.setPreferredWidth(400);
            }
        }

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JTextArea exportField = new JTextArea(6, 0);
        exportField.setEditable(false);
        exportField.setLineWrap(true);
        exportField.setWrapStyleWord(true);
        JScrollPane exportScroll = new JScrollPane(exportField);
        exportScroll.setPreferredSize(new Dimension(0, 120));

        exportButton.addActionListener(e -> {
            update_parser();
            String urlParams = buildUrlParams(parser.mapped_requests);
            String targetUrl = targetUrlField.getText().trim();
            String exportText = urlParams;
            if (!targetUrl.isEmpty() && !urlParams.isEmpty()) {
                String separator = targetUrl.contains("?") ? "&" : "?";
                exportText = targetUrl + separator + urlParams;
            } else if (!targetUrl.isEmpty()) {
                exportText = targetUrl;
            }
            exportField.setText(exportText);
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(exportText), null);
        });

        panel.add(exportScroll, BorderLayout.SOUTH);

        montoyaApi.userInterface().registerSuiteTab("Parameter Collector", panel);
    }

    private void update_parser() {
        String target_url = targetUrlField.getText().trim();
        Runnable refreshTable = () -> SwingUtilities.invokeLater(tableModel::refresh);
        Proxy proxy = montoyaApi.proxy();
        List<ProxyHttpRequestResponse> history = proxy.history();
        for (ProxyHttpRequestResponse reqresp : history) {
            HttpRequest req = reqresp.request();
            if (req.url().contains(target_url)) {
                parser.parse_proxy_http(req);
            }
        }
        refreshTable.run();
    }

    private static String buildUrlParams(Map<String, Set<String>> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder builder = new StringBuilder();
        for (String key : keys) {
            Set<String> values = params.get(key);
            if (values == null) {
                continue;
            }
            for (String value : values) {
                if (builder.length() > 0) {
                    builder.append('&');
                }
                builder.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
                builder.append('=');
                builder.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            }
        }
        return builder.toString();
    }
}