package kr.co.wincom.sjc;

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.jcef.JBCefBrowser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Statement;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import kr.co.wincom.sjc.type.MethodType;
import org.apache.commons.lang.StringUtils;

public class UrlListForm {
    CompareForm compareForm;

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

    public UrlListForm(CompareForm compareForm) {
        this.compareForm = compareForm;

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
            if(StringUtils.isBlank(this.txtTitle.getText())) {
                this.txtTitle.requestFocus();
                return;
            }

            this.btnInsert.setEnabled(false);

            String title = this.txtTitle.getText();
            String method = (String) this.cbMethod.getSelectedItem();
            String leftUrl = this.txtLeftUrl.getText();
            String rightUrl = this.txtRightUrl.getText();
            String bodyData = this.taBodyData.getText();

            Object[] row = {title, method, leftUrl, rightUrl, bodyData};

            DefaultTableModel dfTableModel = (DefaultTableModel) this.urlTable.getModel();
            dfTableModel.insertRow(0, row);
            this.xmlFileSave(dfTableModel);

            this.btnInsert.setEnabled(true);
            this.urlTable.clearSelection();
            this.txtTitle.requestFocus();
            this.clear();
        });

        // Close Button
        this.btnClose.addActionListener(e -> {
            this.dialog.dispose();
        });

        // URL Table
        this.urlTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                DefaultTableModel model = (DefaultTableModel) urlTable.getModel();
                int sRow = urlTable.getSelectedRow();

                String title = (String)model.getValueAt(sRow, 0);
                String method = (String)model.getValueAt(sRow, 1);
                String leftUrl = (String)model.getValueAt(sRow, 2);
                String rightUrl = (String)model.getValueAt(sRow, 3);
                String bodyData = (String)model.getValueAt(sRow, 4);

                txtTitle.setText(title);
                cbMethod.setSelectedItem(method);
                txtLeftUrl.setText(leftUrl);
                txtRightUrl.setText(rightUrl);
                taBodyData.setText(bodyData);
            }
        });
    }

    public void init(String method, String leftUrl, String rightUrl, String bodyData) {
        this.cbMethod.addItem(MethodType.GET.getCode());
        this.cbMethod.addItem(MethodType.POST.getCode());
        this.cbMethod.addItem(MethodType.PUT.getCode());
        this.cbMethod.addItem(MethodType.PATCH.getCode());
        this.cbMethod.addItem(MethodType.DELETE.getCode());

        this.xmlFileRead();

        this.txtTitle.setText("");
        this.cbMethod.setSelectedItem(method);
        this.txtLeftUrl.setText(leftUrl);
        this.txtRightUrl.setText(rightUrl);
        this.taBodyData.setText(bodyData);

        this.dialog.setTitle("URL List");
        this.dialog.setModal(true);
        this.dialog.add(this.mainPanel, BorderLayout.CENTER);
        this.dialog.pack();
        this.dialog.setSize(800, 600);
        this.dialog.setLocation(600, 200);
        this.dialog.setVisible(true);
    }

    private void clear() {
        this.txtTitle.setText("");
        this.cbMethod.setSelectedItem(MethodType.GET.getCode());
        this.txtLeftUrl.setText("");
        this.txtRightUrl.setText("");
        this.taBodyData.setText("");
    }

    // JTable 의 내용을 XML 파일로 저장
    private void xmlFileSave(DefaultTableModel defaultTableModel) {
        String userHome = System.getProperty("user.home");

        try {
            Path dirPath = Paths.get(userHome + "/simpleJsonCompare");
            Path xmlPath = Paths.get(userHome + "/simpleJsonCompare/simpleJsonCompare.xml");

            if (!Files.exists(xmlPath)) {
                Files.createDirectory(dirPath);
                Files.createFile(xmlPath);
            }

            try (XMLEncoder xe = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(xmlPath.toFile())))) {
                Class<DefaultTableModel> clz = DefaultTableModel.class;
                xe.setPersistenceDelegate(clz, new DefaultTableModelPersistenceDelegate());

                xe.writeObject(clz.cast(defaultTableModel));
            }
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionMsg = sw.toString();

            Messages.showMessageDialog(exceptionMsg, "Error", Messages.getErrorIcon());
        }
    }

    private void xmlFileRead() {
        String userHome = System.getProperty("user.home");

        try {
            Path xmlPath = Paths.get(userHome + "/simpleJsonCompare/simpleJsonCompare.xml");

            if (!Files.exists(xmlPath)) {
                return;
            }

            try (XMLDecoder xd = new XMLDecoder(new BufferedInputStream(new FileInputStream(xmlPath.toFile())))) {
                DefaultTableModel model = (DefaultTableModel) xd.readObject();
                this.urlTable.setModel(model);
            }
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionMsg = sw.toString();

            Messages.showMessageDialog(exceptionMsg, "Error", Messages.getErrorIcon());
        }
    }

    // https://github.com/aterai/java-swing-tips/blob/master/PersistenceDelegate/src/java/example/MainPanel.java
    class DefaultTableModelPersistenceDelegate extends DefaultPersistenceDelegate {
        @Override
        protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder encoder) {
            super.initialize(type, oldInstance,  newInstance, encoder);

            DefaultTableModel m = (DefaultTableModel) oldInstance;

            for (int row = 0; row < m.getRowCount(); row++) {
                for (int col = 0; col < m.getColumnCount(); col++) {
                    Object[] o = new Object[] {m.getValueAt(row, col), row, col};
                    encoder.writeStatement(new Statement(oldInstance, "setValueAt", o));
                }
            }
        }
    }
}
