package com.lantingeee.ideaplugin.process;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.generate.config.FilterPattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MethodProcessor {

    @NotNull
    public static PsiMethod[] filterAvailableMethods(PsiClass clazz, @NotNull FilterPattern pattern) {

        List<PsiMethod> availableMethods = new ArrayList();
        collectAvailableMethods(clazz, clazz, pattern, availableMethods, new HashSet());
        PsiMethod[] var10000 = (PsiMethod[]) availableMethods.toArray(PsiMethod.EMPTY_ARRAY);


        return var10000;
    }

    private static void collectAvailableMethods(PsiClass clazz, PsiClass base, @NotNull FilterPattern pattern, List<? super PsiMethod> availableMethods, HashSet<? super PsiClass> visited) {

        PsiMethod[] methods = clazz.getMethods();
        PsiMethod[] var7 = methods;
        int var8 = methods.length;

        for (int var9 = 0; var9 < var8; ++var9) {
            PsiMethod method = var7[var9];
            if (method.hasModifierProperty("public") && !method.hasModifierProperty("static") && !method.hasModifierProperty("abstract")) {
                availableMethods.add(method);
            }

        }
    }
}
