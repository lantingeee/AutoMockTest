package com.lantingeee.ideaplugin.handler;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.util.MemberChooser;
import com.intellij.java.JavaBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleSettings;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.generate.tostring.GenerateToStringClassFilter;
import org.jetbrains.java.generate.*;
import org.jetbrains.java.generate.config.Config;
import org.jetbrains.java.generate.config.ConflictResolutionPolicy;
import org.jetbrains.java.generate.template.TemplateResource;
import org.jetbrains.java.generate.template.toString.ToStringTemplatesManager;

import java.util.Arrays;
import java.util.Collection;

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
                PsiClass clazz = (PsiClass) PsiTreeUtil.getParentOfType(context, PsiClass.class, false);
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

    private static void doExecuteAction(@NotNull Project project, @NotNull final PsiClass clazz, Editor editor) {

        if (FileModificationService.getInstance().preparePsiElementsForWrite(new PsiElement[]{clazz})) {
//            logger.debug("+++ doExecuteAction - START +++");
//            if (logger.isDebugEnabled()) {
//                logger.debug("Current project " + project.getName());
//            }

            PsiElementClassMember[] dialogMembers = buildMembersToShow(clazz);
            GenerateToStringActionHandlerImpl.MemberChooserHeaderPanel header = new GenerateToStringActionHandlerImpl.MemberChooserHeaderPanel(clazz);
            logger.debug("Displaying member chooser dialog");

            MemberChooser<PsiElementClassMember> chooser = new MemberChooser<PsiElementClassMember>(dialogMembers, true, true, project, PsiUtil.isLanguageLevel5OrHigher(clazz), header) {
                @Nullable
                protected String getHelpId() {
                    return "editing.altInsert.tostring";
                }

                protected boolean isInsertOverrideAnnotationSelected() {
                    return JavaCodeStyleSettings.getInstance(clazz.getContainingFile()).INSERT_OVERRIDE_ANNOTATION;
                }
            };
            chooser.setTitle("Select Methods");
            chooser.setCopyJavadocVisible(false);
            chooser.selectElements(getPreselection(clazz, dialogMembers));
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

//            logger.debug("+++ doExecuteAction - END +++");
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


    public static PsiElementClassMember[] buildMembersToShow(PsiClass clazz) {
        Config config = GenerateToStringContext.getConfig();
        PsiField[] filteredFields = GenerateToStringUtils.filterAvailableFields(clazz, true, config.getFilterPattern());
//        if (logger.isDebugEnabled()) {
//            logger.debug("Number of fields after filtering: " + filteredFields.length);
//        }

        PsiMethod[] filteredMethods;
        if (config.enableMethods) {
            filteredMethods = GenerateToStringUtils.filterAvailableMethods(clazz, config.getFilterPattern());
//            if (logger.isDebugEnabled()) {
//                logger.debug("Number of methods after filtering: " + filteredMethods.length);
//            }
        } else {
            filteredMethods = PsiMethod.EMPTY_ARRAY;
        }

        return GenerationUtil.combineToClassMemberList(filteredFields, filteredMethods);
    }

}
