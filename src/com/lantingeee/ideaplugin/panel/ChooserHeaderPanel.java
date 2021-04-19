package com.lantingeee.ideaplugin.panel;

import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.java.JavaBundle;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.options.TabbedConfigurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleListCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.generate.GenerateToStringActionHandlerImpl;
import org.jetbrains.java.generate.GenerateToStringConfigurable;
import org.jetbrains.java.generate.template.TemplateResource;
import org.jetbrains.java.generate.template.toString.ToStringTemplatesManager;
import org.jetbrains.java.generate.view.TemplatesPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ChooserHeaderPanel extends JPanel {
    
    private MemberChooser<PsiElementClassMember> chooser;
    
    private final JComboBox<TemplateResource> comboBox;

    public void setChooser(MemberChooser chooser) {
        this.chooser = chooser;
    }

    public ChooserHeaderPanel(final PsiClass clazz) {
        super(new GridBagLayout());
        Collection<TemplateResource> templates = ToStringTemplatesManager.getInstance().getAllTemplates();
        TemplateResource[] all = (TemplateResource[])templates.toArray(new TemplateResource[0]);
        JButton settingsButton = new JButton(JavaBundle.message("button.text.settings", new Object[0]));
        settingsButton.setMnemonic(83);
        this.comboBox = new ComboBox(all);
        JavaPsiFacade instance = JavaPsiFacade.getInstance(clazz.getProject());
        GlobalSearchScope resolveScope = clazz.getResolveScope();
        ListCellRenderer<TemplateResource> renderer = SimpleListCellRenderer.create((label, value, index) -> {
            label.setText(value.getName());
            String className = value.getClassName();
            if (className != null && instance.findClass(className, resolveScope) == null) {
                this.setForeground(JBColor.RED);
            }

        });
        this.comboBox.setRenderer(renderer);
        settingsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final TemplatesPanel ui = new TemplatesPanel(clazz.getProject());
                Configurable composite = new TabbedConfigurable() {
                    @NotNull
                    protected java.util.List<Configurable> createConfigurables() {
                        List<Configurable> res = new ArrayList();
                        res.add(new GenerateToStringConfigurable(clazz.getProject()));
                        res.add(ui);
                        if (res == null) {
//                            $$$reportNull$$$0(0);
                        }

                        return res;
                    }

                    public String getDisplayName() {
                        return JavaBundle.message("generate.tostring.tab.title", new Object[0]);
                    }

                    public String getHelpTopic() {
                        return "editing.altInsert.tostring.settings";
                    }

                    public void apply() throws ConfigurationException {
                        super.apply();
                        GenerateToStringActionHandlerImpl.updateDialog(clazz, ChooserHeaderPanel.this.chooser);
                        ChooserHeaderPanel.this.comboBox.removeAllItems();
                        Iterator var1 = ToStringTemplatesManager.getInstance().getAllTemplates().iterator();

                        while(var1.hasNext()) {
                            TemplateResource resource = (TemplateResource)var1.next();
                            ChooserHeaderPanel.this.comboBox.addItem(resource);
                        }

                        ChooserHeaderPanel.this.comboBox.setSelectedItem(ToStringTemplatesManager.getInstance().getDefaultTemplate());
                    }
                };
                ShowSettingsUtil.getInstance().editConfigurable(ChooserHeaderPanel.this, composite, () -> {
                    ui.selectItem(ToStringTemplatesManager.getInstance().getDefaultTemplate());
                });
                composite.disposeUIResources();
            }
        });
        this.comboBox.setSelectedItem(ToStringTemplatesManager.getInstance().getDefaultTemplate());
        JLabel templatesLabel = new JLabel(JavaBundle.message("generate.tostring.template.label", new Object[0]));
        templatesLabel.setDisplayedMnemonic('T');
        templatesLabel.setLabelFor(this.comboBox);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = 256;
        constraints.gridx = 0;
        this.add(templatesLabel, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1.0D;
        constraints.fill = 2;
        this.add(this.comboBox, constraints);
        constraints.gridx = 2;
        constraints.weightx = 0.0D;
        this.add(settingsButton, constraints);
    }

    public TemplateResource getSelectedTemplate() {
        return (TemplateResource)this.comboBox.getSelectedItem();
    }
}