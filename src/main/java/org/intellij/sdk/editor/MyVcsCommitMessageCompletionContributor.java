package org.intellij.sdk.editor;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.ui.CommitMessage;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.ElementPatternCondition;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.TextFieldWithAutoCompletionListProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Dmitry Avdeev
 */
public class MyVcsCommitMessageCompletionContributor extends CompletionContributor {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {

        PsiFile file = parameters.getOriginalFile();
        Project project = file.getProject();
        Document document = PsiDocumentManager.getInstance(project).getDocument(file);
        if (document == null) {
            return;
        }

        Supplier<Iterable<Change>> changesSupplier = document.getUserData(CommitMessage.CHANGES_SUPPLIER_KEY);
        if (changesSupplier == null) {
            return;
        }

        result.stopHere();
        int count = parameters.getInvocationCount();

        String text = parameters.getEditor().getDocument().getText();
        String prefix = TextFieldWithAutoCompletionListProvider.getCompletionPrefix(parameters);
        System.out.println(prefix);
        String prefixToMath;

        CompletionResultSet completionResultSet;
        if (endWithIgnoreCase(prefix, "#BUG")
                || endWithIgnoreCase(prefix, "#B")
                || endWithIgnoreCase(prefix, "#BU")) {
            prefixToMath = "#BUG";

            completionResultSet = result.withPrefixMatcher(new PlainPrefixMatcher(prefixToMath, false));

            LookupElement lookupElement = LookupElementBuilder
                    .create("这是一个BUG(12798345789)", "这是一个BUG(12798345789)")
                    .withLookupString("#BUG")
                    .withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE);
            completionResultSet.addElement(lookupElement);


            completionResultSet.restartCompletionOnPrefixChange(new ElementPattern<String>() {
                @Override
                public boolean accepts(@Nullable Object o) {
                    return o instanceof String;
                }

                @Override
                public boolean accepts(@Nullable Object o, ProcessingContext context) {
                    return endWithIgnoreCase((String) o, "#BUG")
                            || endWithIgnoreCase((String) o, "#B")
                            || endWithIgnoreCase((String) o, "#BU");
                }

                @Override
                public ElementPatternCondition<String> getCondition() {
                    return null;
                }
            });

        } else if (text.length() < MAX_LENGTH) {
            completionResultSet = result.withPrefixMatcher(new PlainPrefixMatcher(prefix, true));
            List<LookupElement> lookupElements = Arrays.stream(ARR).map(s -> LookupElementBuilder
                    .create(s)
                    .bold().withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE)).collect(Collectors.toList());
            completionResultSet.addAllElements(lookupElements);
        }


    }

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        System.out.println("beforeCompletion");
    }

    @Override
    public void duringCompletion(@NotNull CompletionInitializationContext context) {
        System.out.println("duringCompletion");
    }


    //写一个判断字符串后缀匹配并且忽略大小写的方法
    private static boolean endWithIgnoreCase(String str, String suffix) {
        if (str == null || suffix == null) {
            return false;
        }
        if (str.endsWith(suffix)) {
            return true;
        }
        if (str.length() < suffix.length()) {
            return false;
        }
        return str.substring(str.length() - suffix.length()).equalsIgnoreCase(suffix);
    }

    private static final String[] ARR = new String[]{"chore:", "fix:", "build:", "style:", "feat:"};

    private static final int MAX_LENGTH = Arrays.stream(ARR).mapToInt(String::length).max().getAsInt();
}