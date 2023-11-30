package kr.co.wincom.sjc;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.jcef.JBCefBrowser;
import kr.co.wincom.sjc.dto.ResultDto;
import kr.co.wincom.sjc.service.HttpService;
import kr.co.wincom.sjc.type.DialogToolWindowType;
import kr.co.wincom.sjc.type.MethodType;
import kr.co.wincom.sjc.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CompareForm {
    private DialogToolWindowType dialogToolWindowType;
    private JDialog dialog;

    private JBCefBrowser jbCefBrowser;
    private JPanel mainPanel;
    private JComboBox cbMethod;
    private JTextField txtLeftUrl;
    private JTextField txtRightUrl;
    private JTextArea taBodyData;
    private JButton btnCompare;
    private JPanel webviewPanel;
    private JButton btnUrlList;

    public CompareForm(DialogToolWindowType dialogToolWindowType) {
        this.dialogToolWindowType = dialogToolWindowType;

        this.mouseEvent();

        this.btnCompare.addActionListener(actionEvent -> {
            if (!this.validation()) {
                return;
            }

            SwingUtilities.invokeLater(() -> execute());
        });

        this.txtLeftUrl.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!validation()) {
                        return;
                    }

                    SwingUtilities.invokeLater(() -> execute());
                }
            }
        });

        this.txtRightUrl.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!validation()) {
                        return;
                    }

                    SwingUtilities.invokeLater(() -> execute());
                }
            }
        });

        this.btnUrlList.addActionListener(actionEvent -> {
            UrlListForm urlListForm = new UrlListForm(this);
            urlListForm.init((String) this.cbMethod.getSelectedItem(), this.txtLeftUrl.getText(), this.txtRightUrl.getText(), this.taBodyData.getText());
        });
    }

    public void init() {
        this.cbMethod.addItem(MethodType.GET.getCode());
        this.cbMethod.addItem(MethodType.POST.getCode());
        this.cbMethod.addItem(MethodType.PUT.getCode());
        this.cbMethod.addItem(MethodType.PATCH.getCode());
        this.cbMethod.addItem(MethodType.DELETE.getCode());

        SwingUtilities.invokeLater(() -> {
            this.jbCefBrowser = new JBCefBrowser();
            JComponent jComponent = this.jbCefBrowser.getComponent();
            jComponent.setBackground(Color.WHITE);
            this.webviewPanel.add(jComponent, BorderLayout.CENTER);
            this.jbCefBrowser.loadHTML(""); // 이렇게 해야 처음 로딩했을 때 JTextField 에 값을 입력할 수 있음.

            this.txtLeftUrl.requestFocus();
        });

        if (this.dialogToolWindowType.equals(DialogToolWindowType.JDIALOG)) {
            this.dialog = new JDialog();
            this.dialog.setTitle("Simple Json Compare");
            this.dialog.setModal(true);
            this.dialog.add(this.mainPanel, BorderLayout.CENTER);
            this.dialog.pack();
            this.dialog.setSize(800, 700);
            this.dialog.setLocation(500, 100);
            this.dialog.setVisible(true);
        }
    }

    public JPanel getMainPanel() {
        return this.mainPanel;
    }

    private boolean validation() {
        if (this.txtLeftUrl.getText().isBlank()) {
            this.txtLeftUrl.requestFocus();
            return false;
        }

        if (this.txtRightUrl.getText().isBlank()) {
            this.txtRightUrl.requestFocus();
            return false;
        }

        this.txtLeftUrl.setEnabled(false);
        this.txtRightUrl.setEnabled(false);

        this.jbCefBrowser.loadHTML("");

        this.btnCompare.setEnabled(false);
        this.btnCompare.setText("Wait......");

        return true;
    }

    private void execute() {
        boolean isSame = true;
        String method = (String) this.cbMethod.getSelectedItem();
        String leftUrl = this.txtLeftUrl.getText();
        String rightUrl = this.txtRightUrl.getText();
        String bodyData = this.taBodyData.getText();

        StringBuilder leftSb = new StringBuilder();
        StringBuilder rightSb = new StringBuilder();

        try {
            CommonUtils.checkUrl(leftUrl);
            CommonUtils.checkUrl(rightUrl);

            HashMap<String, String> hm = this.getData(method, leftUrl, rightUrl, bodyData);
            String leftPrettyData = CommonUtils.makeJsonPrettyData(hm.get("leftData"));
            String rightPrettyData = CommonUtils.makeJsonPrettyData(hm.get("rightData"));

            String[] arrLeftData = StringUtils.splitByWholeSeparatorPreserveAllTokens(leftPrettyData, "\n");
            String[] arrRightData = StringUtils.splitByWholeSeparatorPreserveAllTokens(rightPrettyData, "\n");

            DiffRowGenerator generator = DiffRowGenerator.create()
                    .showInlineDiffs(true)
                    .inlineDiffByWord(true)
                    .oldTag(f -> "")
                    .newTag(f -> "")
                    .build();

            List<DiffRow> diffRowList = generator.generateDiffRows(Arrays.asList(arrLeftData), Arrays.asList(arrRightData));

            for (DiffRow row : diffRowList) {
                String leftDiffTrimData = row.getOldLine().trim();
                String rightDiffTrimData = row.getNewLine().trim();
                String leftDiffData = row.getOldLine();
                String rightDiffData = row.getNewLine();

                if (StringUtils.isBlank(leftDiffData)) {
                    leftDiffData = rightDiffData.replace(rightDiffTrimData, "") + "(NO DATA)";
                }

                if (StringUtils.isBlank(rightDiffData)) {
                    rightDiffData = leftDiffData.replace(leftDiffTrimData, "") + "(NO DATA)";
                }

                if (leftDiffData.equals(rightDiffData)) {
                    leftSb.append(CommonUtils.replaceColor(leftDiffData) + "<br>");
                    rightSb.append(CommonUtils.replaceColor(rightDiffData) + "<br>");
                } else {
                    isSame = false;
                    leftSb.append("<font color='#DF01D7'>" + leftDiffData.replace(" ", "&nbsp;").replace("<", "&lt;").replace(">", "&gt;") + "</font><br>");
                    rightSb.append("<font color='#DF01D7'>" + rightDiffData.replace(" ", "&nbsp;").replace("<", "&lt;").replace(">", "&gt;") + "</font><br>");
                }
            }

            this.makeHtml(leftSb.toString(), rightSb.toString());

            if (!isSame) {
                SwingUtilities.invokeLater(() -> Messages.showMessageDialog("different", "", Messages.getInformationIcon()));
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionMsg = sw.toString();

            Messages.showMessageDialog(exceptionMsg, "Error", Messages.getErrorIcon());
        } finally {
            this.btnCompare.setEnabled(true);
            this.btnCompare.setText("Compare");
            this.webviewPanel.requestFocus();

            this.txtLeftUrl.setEnabled(true);
            this.txtRightUrl.setEnabled(true);
        }
    }

    private HashMap<String, String> getData(String method, String leftUrl, String rightUrl, String bodyData) throws Exception {
        HashMap<String, String> hm = new HashMap<>();
        HttpService httpService = HttpService.getInstance();

        CompletableFuture<ResultDto> leftCf = httpService.call(method, leftUrl, bodyData);
        CompletableFuture<ResultDto> rightCf = httpService.call(method, CommonUtils.makeUrl(leftUrl, rightUrl), bodyData);

        while (true) {
            Thread.sleep(1);

            if (leftCf.isDone() && rightCf.isDone()) {
                break;
            }
        }

        ResultDto leftResultDto = leftCf.get();
        ResultDto rightResultDto = rightCf.get();

        if (leftResultDto.getErrorMsg() != null) {
            throw new Exception(leftResultDto.getErrorMsg());
        }

        if (rightResultDto.getErrorMsg() != null) {
            throw new Exception(rightResultDto.getErrorMsg());
        }

        hm.put("leftData", leftResultDto.getResData());
        hm.put("rightData", rightResultDto.getResData());

        return hm;
    }

    private void makeHtml(String leftData, String rightData) {
        String html = "<!doctype html>\n" +
                "<html lang='ko'>\n" +
                "<head>\n" +
                "    <meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>\n" +
                "\n" +
                "    <style>\n" +
                "    .table2 {\n" +
                "      border-collapse: collapse;\n" +
                "      table-layout: fixed;\n" +
                "      word-break: break-all;\n" +
                "      overflow: auto;\n" +
                "      margin-left: auto;\n" +
                "      margin-right: auto;\n" +
                "    }\n" +
                "\n" +
                "    .table2 th {\n" +
                "      border: 1px solid black;\n" +
                "      background-color: #F0F8FF;\n" +
                "    }\n" +
                "\n" +
                "    .table2 td {\n" +
                "      border: 1px solid black;\n" +
                "      vertical-align: top;\n" +
                "    }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<table style='width:100%' class='table2'>\n" +
                "    <thead>\n" +
                "    <tr align='center'>\n" +
                "        <th style='width:50%'>LEFT</th>\n" +
                "        <th style='width:50%'>RIGHT</th>\n" +
                "    </tr>\n" +
                "    </thead>\n" +
                "    <tbody>\n" +
                "       <tr>\n" +
                "           <td>" + leftData + "</td>\n" +
                "           <td>" + rightData + "</td>\n" +
                "       </tr>\n" +
                "    </tbody>\n" +
                "</table>" +
                "</body>\n" +
                "</html>";

        this.jbCefBrowser.loadHTML(html);
    }

    private void mouseEvent() {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item = new JMenuItem(new DefaultEditorKit.CutAction());
        item.setText("Cut");
        popup.add(item);
        item = new JMenuItem(new DefaultEditorKit.CopyAction());
        item.setText("Copy");
        popup.add(item);
        item = new JMenuItem(new DefaultEditorKit.PasteAction());
        item.setText("Paste");
        popup.add(item);

        this.txtLeftUrl.setComponentPopupMenu(popup);
        this.txtRightUrl.setComponentPopupMenu(popup);
        this.taBodyData.setComponentPopupMenu(popup);

        this.txtLeftUrl.addMouseListener(new MouseListener(1));
        this.txtRightUrl.addMouseListener(new MouseListener(2));
        this.taBodyData.addMouseListener(new MouseListener(3));
    }

    class MouseListener implements MouseInputListener {

        private int number;

        public MouseListener(int number) {
            this.number = number;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (this.number == 1) {
                txtLeftUrl.requestFocus();
            } else if (this.number == 2) {
                txtRightUrl.requestFocus();
            } else if (this.number == 3) {
                taBodyData.requestFocus();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }
    }
}



























