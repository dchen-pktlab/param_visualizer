import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        montoyaApi.extension().setName("Parameter Collector");

        RequestParser parser = new RequestParser();
        RequestTable tableModel = new RequestTable(parser.mapped_requests);

        Runnable refreshTable = () -> SwingUtilities.invokeLater(tableModel::refresh);
        RequestHandler handler = new RequestHandler(parser, refreshTable);
        montoyaApi.proxy().registerRequestHandler(handler);

        Proxy proxy = montoyaApi.proxy();
        List<ProxyHttpRequestResponse> history = proxy.history();
        for (ProxyHttpRequestResponse reqresp : history) {
            HttpRequest req = reqresp.request();
            parser.parse_proxy_http(req);
        }
        refreshTable.run();

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel title = new JLabel("Parameter Collector", SwingConstants.CENTER);
        panel.add(title, BorderLayout.NORTH);

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

        JTextField exportField = new JTextField();
        exportField.setEditable(false);

        JButton exportButton = new JButton("Build URL Params");
        exportButton.addActionListener(e -> {
            String urlParams = buildUrlParams(parser.mapped_requests);
            exportField.setText(urlParams);
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(urlParams), null);
        });

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 0));
        bottomPanel.add(exportField, BorderLayout.CENTER);
        bottomPanel.add(exportButton, BorderLayout.EAST);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        montoyaApi.userInterface().registerSuiteTab("Parameter Collector", panel);
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