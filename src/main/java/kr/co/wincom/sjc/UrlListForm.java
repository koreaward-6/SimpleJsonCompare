package kr.co.wincom.sjc;

import com.intellij.openapi.ui.Messages;
import kr.co.wincom.sjc.type.MethodType;
import kr.co.wincom.sjc.util.CommonUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.PatternSyntaxException;

public class UrlListForm {

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
    private JTextField txtSearchWord;

    private CompareForm compareForm;

    private JDialog dialog = new JDialog();
    private TableRowSorter<TableModel> rowSorter = new TableRowSorter<>();

    public UrlListForm(CompareForm compareForm) {
        this.compareForm = compareForm;

        this.urlTable.setRowSorter(this.rowSorter);
        this.urlTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.urlTable.setDefaultEditor(Object.class, null);

        DefaultTableModel model = (DefaultTableModel) this.urlTable.getModel();
        model.addColumn("Title");
        model.addColumn("Method");
        model.addColumn("Left URL");
        model.addColumn("Right URL");
        model.addColumn("Body");

        this.columnResize();
        this.rowSorter.setModel(model);

        // 이렇게 해야 팝업창 띄웠을 때 Title TextField에 포커스가 간다.
        this.dialog.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                txtTitle.requestFocus();
            }
        });

        // https://stackoverflow.com/questions/22066387/how-to-search-an-element-in-a-jtable-java
        // https://velog.io/@jipark09/Java-Swing-JTable-%EC%9E%90%EB%8F%99%EA%B2%80%EC%83%89-%ED%96%89-%EC%84%A0%ED%83%9D
        this.txtSearchWord.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                search();
            }

            private void search() {
                String text = txtSearchWord.getText();

                if (text.isBlank()) {
                    rowSorter.setRowFilter(null);
                } else {
                    try {
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    } catch (PatternSyntaxException ex) {
                        rowSorter.setRowFilter(null);
                    }
                }
            }
        });

        // Insert Button
        this.btnInsert.addActionListener(e -> {
            if (CommonUtils.isBlank(this.txtTitle.getText())) {
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

            this.rowSorter.setModel(dfTableModel);
            this.urlTable.clearSelection();
            this.txtTitle.requestFocus();
            this.clear();
            this.btnInsert.setEnabled(true);
        });

        // Update Button
        this.btnUpdate.addActionListener(e -> {
            int selectedRow = this.urlTable.getSelectedRow();

            if (selectedRow < 0) {
                return;
            }

            if (CommonUtils.isBlank(this.txtTitle.getText())) {
                this.txtTitle.requestFocus();
                return;
            }

            this.btnUpdate.setEnabled(false);

            DefaultTableModel dfTableModel = (DefaultTableModel) this.urlTable.getModel();

            int modelRow = this.urlTable.convertRowIndexToModel(selectedRow);
            dfTableModel.setValueAt(this.txtTitle.getText(), modelRow, 0);
            dfTableModel.setValueAt(this.cbMethod.getSelectedItem(), modelRow, 1);
            dfTableModel.setValueAt(this.txtLeftUrl.getText(), modelRow, 2);
            dfTableModel.setValueAt(this.txtRightUrl.getText(), modelRow, 3);
            dfTableModel.setValueAt(this.taBodyData.getText(), modelRow, 4);
            this.xmlFileSave(dfTableModel);

            this.rowSorter.setModel(dfTableModel);
            this.txtTitle.requestFocus();
            this.btnUpdate.setEnabled(true);
        });

        // Delete Button
        this.btnDelete.addActionListener(e -> {
            int selectedRow = this.urlTable.getSelectedRow();

            if (selectedRow < 0) {
                return;
            }

            int modelRow = this.urlTable.convertRowIndexToModel(selectedRow);

            DefaultTableModel dfTableModel = (DefaultTableModel) this.urlTable.getModel();
            String title = (String) dfTableModel.getValueAt(modelRow, 0);

            int retVal = JOptionPane.showConfirmDialog(this.dialog, title + ", delete?");
            if (retVal != 0) { // 0=yes, 1=no, 2=cancel
                return;
            }

            this.btnDelete.setEnabled(false);

            dfTableModel.removeRow(modelRow);
            this.xmlFileSave(dfTableModel);

            this.rowSorter.setModel(dfTableModel);
            this.urlTable.clearSelection();
            this.txtTitle.requestFocus();
            this.clear();
            this.btnDelete.setEnabled(true);
        });

        // Put Button
        this.btnPut.addActionListener(e -> {
            String method = (String) this.cbMethod.getSelectedItem();
            String leftUrl = this.txtLeftUrl.getText();
            String rightUrl = this.txtRightUrl.getText();
            String bodyData = this.taBodyData.getText();

            if (CommonUtils.isBlank(leftUrl) && CommonUtils.isBlank(rightUrl)) {
                return;
            }

            this.compareForm.putData(method, leftUrl, rightUrl, bodyData);
            this.dialog.dispose();
        });

        // Close Button
        this.btnClose.addActionListener(e -> {
            this.dialog.dispose();
        });

        // URL Table mousePressed
        this.urlTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                DefaultTableModel model = (DefaultTableModel) urlTable.getModel();
                int sRow = urlTable.getSelectedRow();
                int modelRow = urlTable.convertRowIndexToModel(sRow);

                if (modelRow < 0) {
                    return;
                }

                String title = (String) model.getValueAt(modelRow, 0);
                String method = (String) model.getValueAt(modelRow, 1);
                String leftUrl = (String) model.getValueAt(modelRow, 2);
                String rightUrl = (String) model.getValueAt(modelRow, 3);
                String bodyData = (String) model.getValueAt(modelRow, 4);

                txtTitle.setText(title);
                cbMethod.setSelectedItem(method);
                txtLeftUrl.setText(leftUrl);
                txtRightUrl.setText(rightUrl);
                taBodyData.setText(bodyData);
            }
        });

        // 방향키 및 페이지업키, 페이지다운키 눌렀을 때
        this.urlTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN ||
                        e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                    DefaultTableModel model = (DefaultTableModel) urlTable.getModel();
                    int sRow = urlTable.getSelectedRow();
                    int modelRow = urlTable.convertRowIndexToModel(sRow);

                    if (modelRow < 0) {
                        return;
                    }

                    String title = (String) model.getValueAt(modelRow, 0);
                    String method = (String) model.getValueAt(modelRow, 1);
                    String leftUrl = (String) model.getValueAt(modelRow, 2);
                    String rightUrl = (String) model.getValueAt(modelRow, 3);
                    String bodyData = (String) model.getValueAt(modelRow, 4);

                    txtTitle.setText(title);
                    cbMethod.setSelectedItem(method);
                    txtLeftUrl.setText(leftUrl);
                    txtRightUrl.setText(rightUrl);
                    taBodyData.setText(bodyData);
                }
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
        this.dialog.setSize(800, 690);
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

    private void columnResize() {
        this.urlTable.getColumnModel().getColumn(1).setMaxWidth(70);
        DefaultTableCellRenderer celAlignCenter = new DefaultTableCellRenderer();
        celAlignCenter.setHorizontalAlignment(JLabel.CENTER);
        this.urlTable.getColumn("Method").setCellRenderer(celAlignCenter);
    }

    // JTable 의 내용을 XML 파일로 저장
    private void xmlFileSave(DefaultTableModel defaultTableModel) {
        String userHome = System.getProperty("user.home");

        try {
            Path dirPath = Paths.get(userHome + "/simpleJsonCompare");
            Path xmlPath = Paths.get(userHome + "/simpleJsonCompare/simpleJsonCompare.xml");

            if (!Files.exists(dirPath)) {
                Files.createDirectory(dirPath);
            }

            if (!Files.exists(xmlPath)) {
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

                TableColumnModel tableColumnModel = this.urlTable.getColumnModel();
                tableColumnModel.getColumn(0).setHeaderValue("Title");
                tableColumnModel.getColumn(1).setHeaderValue("Method");
                tableColumnModel.getColumn(2).setHeaderValue("Left URL");
                tableColumnModel.getColumn(3).setHeaderValue("Right URL");
                tableColumnModel.getColumn(4).setHeaderValue("Body");

                this.columnResize();
                this.rowSorter.setModel(model);
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
            super.initialize(type, oldInstance, newInstance, encoder);

            DefaultTableModel m = (DefaultTableModel) oldInstance;

            for (int row = 0; row < m.getRowCount(); row++) {
                for (int col = 0; col < m.getColumnCount(); col++) {
                    Object[] o = new Object[]{m.getValueAt(row, col), row, col};
                    encoder.writeStatement(new Statement(oldInstance, "setValueAt", o));
                }
            }
        }
    }
}
