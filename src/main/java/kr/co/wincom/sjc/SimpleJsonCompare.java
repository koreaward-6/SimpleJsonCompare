package kr.co.wincom.sjc;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.jcef.JBCefApp;
import kr.co.wincom.sjc.type.DialogToolWindowType;
import org.jetbrains.annotations.NotNull;

public class SimpleJsonCompare extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (!JBCefApp.isSupported()) {
            Project project = e.getProject();
            Messages.showMessageDialog(project, "JCEF is not supported.", "", Messages.getErrorIcon());
            return;
        }

        CompareForm compareForm = new CompareForm(DialogToolWindowType.JDIALOG);
        compareForm.init();
    }
}
