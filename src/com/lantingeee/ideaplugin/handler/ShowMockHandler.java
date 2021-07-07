package com.lantingeee.ideaplugin.handler;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleSettings;
import com.intellij.psi.util.PsiTreeUtil;
import com.lantingeee.ideaplugin.panel.ChooserHeaderPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.generate.tostring.GenerateToStringClassFilter;
import org.jetbrains.java.generate.GenerateToStringContext;
import org.jetbrains.java.generate.GenerateToStringUtils;
import org.jetbrains.java.generate.GenerationUtil;
import org.jetbrains.java.generate.config.Config;

import java.util.Arrays;

public class ShowMockHandler implements CodeInsightActionHandler {
    private static final Logger logger = Logger.getInstance("#ShowMockHandler");

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {

        PsiClass clazz = getSubjectClass(editor, psiFile);

        assert clazz != null;

        doExecuteAction(project, clazz, editor);
    }

    @Nullable
    private static PsiClass getSubjectClass(Editor editor, PsiFile file) {
        if (file == null) {
            return null;
        } else {
            int offset = editor.getCaretModel().getOffset();
            PsiElement context = file.findElementAt(offset);
            if (context == null) {
                return null;
            } else {

                // 获取当前编辑的文件, 可以进而获取 PsiClass, PsiField 对象
                // 获取Java类或者接口
                PsiClass clazz = getTargetClass(editor, file);

                if (clazz == null) {
                    return null;
                } else {
                    GenerateToStringClassFilter[] var5 = (GenerateToStringClassFilter[])GenerateToStringClassFilter.EP_NAME.getExtensions();
                    int var6 = var5.length;

                    for(int var7 = 0; var7 < var6; ++var7) {
                        GenerateToStringClassFilter filter = var5[var7];
                        if (!filter.canGenerateToString(clazz)) {
                            return null;
                        }
                    }

                    return clazz;
                }
            }
        }
    }

    @Nullable
    public static PsiClass getTargetClass(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return null;
        } else {
            PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
            return target instanceof SyntheticElement ? null : target;
        }
    }

    private static void doExecuteAction(@NotNull Project project, @NotNull final PsiClass clazz, Editor editor) {

        if (FileModificationService.getInstance().preparePsiElementsForWrite(new PsiElement[]{clazz})) {

            PsiElementClassMember[] filedElement = buildMembersToShow(clazz, "Filed");
            ChooserHeaderPanel header = new ChooserHeaderPanel(clazz);
            logger.debug("Displaying member chooser dialog");

            MemberChooser<PsiElementClassMember> chooser = new MemberChooser<PsiElementClassMember>(filedElement, true, true, project, false, header) {
                @Nullable
                protected String getHelpId() {
                    return "editing.altInsert.tostring";
                }

                protected boolean isInsertOverrideAnnotationSelected() {
                    return JavaCodeStyleSettings.getInstance(clazz.getContainingFile()).INSERT_OVERRIDE_ANNOTATION;
                }
            };
            chooser.setTitle("Select Auto Mock Elements");
            chooser.setCopyJavadocVisible(false);
            chooser.selectElements(getPreselection(clazz, filedElement));

            header.setChooser(chooser);
            if (ApplicationManager.getApplication().isUnitTestMode()) {
                chooser.close(0);
            } else {
                chooser.show();
            }

//            if (0 == chooser.getExitCode()) {
//                Collection<PsiMember> selectedMembers = GenerationUtil.convertClassMembersToPsiMembers(chooser.getSelectedElements());
//                TemplateResource template = header.getSelectedTemplate();
//                ToStringTemplatesManager.getInstance().setDefaultTemplate(template);
//                if (template.isValidTemplate()) {
//                    GenerateToStringWorker worker = new GenerateToStringWorker(clazz, editor, chooser.isInsertOverrideAnnotation());
//                    ConflictResolutionPolicy resolutionPolicy = worker.exitsMethodDialog(template);
//
//                    try {
//                        WriteCommandAction.runWriteCommandAction(project, "Generate toString()", (String)null, () -> {
//                            worker.execute(selectedMembers, template, resolutionPolicy);
//                        }, new PsiFile[0]);
//                    } catch (Exception var11) {
//                        GenerationUtil.handleException(project, var11);
//                    }
//                } else {
//                    HintManager.getInstance().showErrorHint(editor, "toString() template '" + template.getFileName() + "' is invalid");
//                }
//            }

            logger.debug("+++ doExecuteAction - END +++");
        }
    }
    private static PsiElementClassMember[] getPreselection(@NotNull PsiClass clazz, PsiElementClassMember[] dialogMembers) {
//        if (clazz == null) {
//            $$$reportNull$$$0(5);
//        }

        return (PsiElementClassMember[]) Arrays.stream(dialogMembers).filter((member) -> {
            return member.getElement().getContainingClass() == clazz;
        }).toArray((x$0) -> {
            return new PsiElementClassMember[x$0];
        });
    }


    public static PsiElementClassMember[] buildMembersToShow(PsiClass clazz, String type) {
        Config config = GenerateToStringContext.getConfig();
        PsiField[] filteredFields = GenerateToStringUtils.filterAvailableFields(clazz, true, config.getFilterPattern());

        PsiMethod[] filteredMethods;
        // 仅选择 filed 属性列表
        if ("Filed".equalsIgnoreCase(type)) {
            return GenerationUtil.combineToClassMemberList(filteredFields, PsiMethod.EMPTY_ARRAY);
        } else if ("Method".equalsIgnoreCase(type)) {
            filteredMethods = GenerateToStringUtils.filterAvailableMethods(clazz, config.getFilterPattern());
            return GenerationUtil.combineToClassMemberList(PsiField.EMPTY_ARRAY, filteredMethods);
        }
        return GenerationUtil.combineToClassMemberList(PsiField.EMPTY_ARRAY, PsiMethod.EMPTY_ARRAY);
    }

}
