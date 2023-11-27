package kr.co.wincom.sjc;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import kr.co.wincom.sjc.type.MethodType;

public class UrlListForm {
    private JDialog dialog = new JDialog();

    private JPanel mainPanel;
    private JButton btnInsert;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnPut;
    private JButton btnClose;
    private JTextField txtTitle;
    private JComboBox cbMethod;
    private JTable urlTable;
    private JTextField txtLeftUrl;
    private JTextField txtRightUrl;
    private JTextArea taBodyData;

    public UrlListForm() {
        DefaultTableModel model = (DefaultTableModel) this.urlTable.getModel();
        model.addColumn("Title");
        model.addColumn("Method");
        model.addColumn("Left URL");
        model.addColumn("Right URL");
        model.addColumn("Body");

        this.urlTable.getColumnModel().getColumn(1).setMaxWidth(70);
        DefaultTableCellRenderer celAlignCenter = new DefaultTableCellRenderer();
        celAlignCenter.setHorizontalAlignment(JLabel.CENTER);
        this.urlTable.getColumn("Method").setCellRenderer(celAlignCenter);

        // Insert Button
        this.btnInsert.addActionListener(e -> {
            this.btnInsert.setEnabled(false);

            String title = "타이틀";
            String method = "PUT";
            String leftUrl = "http://127.0.0.1:8081/aaa?param=a";
            String rightUrl = "http://127.0.0.1:8081";
            String body = "I Love My Body/화사 (HWASA)";

            Object[] row = { title + " - " + model.getRowCount(), method, leftUrl, rightUrl, body};

            model.insertRow(0, row);

            this.btnInsert.setEnabled(true);
        });

        // Close Button
        this.btnClose.addActionListener(e -> {
            this.dialog.dispose();
        });

        // URL Table
        urlTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                DefaultTableModel model = (DefaultTableModel) urlTable.getModel();
                int sRow = urlTable.getSelectedRow();

                String title = (String)model.getValueAt(sRow, 0);
                String method = (String)model.getValueAt(sRow, 1);
                String leftUrl = (String)model.getValueAt(sRow, 2);
                String rightUrl = (String)model.getValueAt(sRow, 3);
                String body = (String)model.getValueAt(sRow, 4);

                txtTitle.setText(title);
                cbMethod.setSelectedItem(method);
                txtLeftUrl.setText(leftUrl);
                txtRightUrl.setText(rightUrl);
                taBodyData.setText(body);
            }
        });
    }

    public void init() {
        this.cbMethod.addItem(MethodType.GET.getCode());
        this.cbMethod.addItem(MethodType.POST.getCode());
        this.cbMethod.addItem(MethodType.PUT.getCode());
        this.cbMethod.addItem(MethodType.PATCH.getCode());
        this.cbMethod.addItem(MethodType.DELETE.getCode());

        this.dialog.setTitle("URL List");
        this.dialog.setModal(true);
        this.dialog.add(this.mainPanel, BorderLayout.CENTER);
        this.dialog.pack();
        this.dialog.setSize(800, 600);
        this.dialog.setLocation(600, 200);
        this.dialog.setVisible(true);
    }
}
