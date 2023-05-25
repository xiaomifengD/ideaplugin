package org.intellij.sdk.editor;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author by shanluo
 * @date 2023/5/25 15:28
 */
public class BugCommitMessageCompletionContributor extends CompletionContributor {


    public BugCommitMessageCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider<CompletionParameters>() {
            @Override
            public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                String prefix = parameters.getOriginalPosition().getText();
                if ("#BUG".equals(prefix)) {
                    resultSet.addElement(LookupElementBuilder.create("BUG 1"));
                    resultSet.addElement(LookupElementBuilder.create("AAAA 2"));
                    // 添加更多的 BUG 信息
                }
            }
        });
    }
}
