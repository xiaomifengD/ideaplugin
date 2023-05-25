package org.intellij.sdk.editor;

import com.intellij.codeInsight.completion.PlainTextSymbolCompletionContributor;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author by shanluo
 * @date 2023/2/15 17:43
 */
public class VcsCommitMessageCompletion implements PlainTextSymbolCompletionContributor {
    @Override
    public @NotNull Collection<LookupElement> getLookupElements(@NotNull PsiFile file, int invocationCount, @NotNull String prefix) {
        System.out.println(prefix);
        System.out.println(file.getText());
//        int textLength = file.getTextLength();
        String[] arr = new String[]{"chore", "fix", "build", "style", "feat"};
        int max = 0;
//
//        if (arr.length > 0) {
//            for (String s : arr) {
//                max = Math.max(max, s.length());
//            }
//        }
//
//        Collection<LookupElement> EMPTY = Collections.EMPTY_LIST;
//        if (max == 0) {
//            return EMPTY;
//        }
//        if (textLength > max) {
//            return EMPTY;
//        }
//        String text = file.getText();

        Collection<LookupElement> lookupElements = new ArrayList<>();
//        for (String s : arr) {
//            if (s.startsWith(text)){
//                LookupElement lookupElement = LookupElementBuilder
//                        .create(s)
//                        .bold().withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE);
//                lookupElements.add(lookupElement);
//            }
//        }
                LookupElement lookupElement = LookupElementBuilder
                        .create("testAUtoComplete")
                        .bold().withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE);
                lookupElements.add(lookupElement);
        return lookupElements;
    }
}
