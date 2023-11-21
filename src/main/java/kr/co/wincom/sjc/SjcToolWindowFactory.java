package kr.co.wincom.sjc;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import kr.co.wincom.sjc.type.DialogToolWindowType;
import org.jetbrains.annotations.NotNull;

public class SjcToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        CompareForm compareForm = new CompareForm(DialogToolWindowType.TOOL_WINDOW_TYPE);
        compareForm.init();
        Content content = ContentFactory.SERVICE.getInstance().createContent(compareForm.getMainPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
