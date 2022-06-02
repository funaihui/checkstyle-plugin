package io.gitlab.arturbosch.detekt.idea.config;

import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class DetektConfigurationForm {

    private JCheckBox enableDetekt;
    private JPanel myMainPanel;

    private DetektConfigStorage detektConfigStorage;
    private final Project project;

    public DetektConfigurationForm(Project project) {
        this.project = project;
    }

    @NotNull
    public JComponent createPanel(@NotNull DetektConfigStorage detektConfigStorage) {
        this.detektConfigStorage = detektConfigStorage;
        myMainPanel.setBorder(IdeBorderFactory.createTitledBorder("Detekt Settings"));
        return myMainPanel;
    }

    public void apply() {
        detektConfigStorage.setEnableDetekt(enableDetekt.isSelected());
    }

    public void reset() {
        enableDetekt.setSelected(detektConfigStorage.getEnableDetekt());
    }

    public boolean isNotModified() {
        return Objects.equals(detektConfigStorage.getEnableDetekt(), enableDetekt.isSelected());
    }
}
