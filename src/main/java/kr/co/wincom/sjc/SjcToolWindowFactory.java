package kr.co.wincom.sjc;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.jcef.JBCefApp;
import kr.co.wincom.sjc.type.DialogToolWindowType;
import org.jetbrains.annotations.NotNull;

public class SjcToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        if (!JBCefApp.isSupported()) {
            Messages.showMessageDialog(project, "JCEF is not supported.", "", Messages.getErrorIcon());
            return;
        }

        CompareForm compareForm = new CompareForm(DialogToolWindowType.TOOL_WINDOW);
        compareForm.init();

        Content content = toolWindow.getContentManager().getFactory().createContent(compareForm.getMainPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
