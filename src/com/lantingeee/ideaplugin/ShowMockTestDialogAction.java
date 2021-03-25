package com.lantingeee.ideaplugin;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.lantingeee.ideaplugin.handler.ShowMockHandler;
import org.jetbrains.annotations.NotNull;


public class ShowMockTestDialogAction extends CodeInsightAction {

    public ShowMockTestDialogAction() {
        super();
    }

    @NotNull
    @Override
    protected CodeInsightActionHandler getHandler() {
        return new ShowMockHandler();
    }

}
