// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.intellij.sdk.editor;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * This is a custom {@link TypedHandlerDelegate} that handles actions activated keystrokes in the editor.
 * The execute method inserts a fixed string at Offset 0 of the document.
 * Document changes are made in the context of a write action.
 */
class MyTypedHandler extends TypedHandlerDelegate {

  @NotNull
  @Override
  public Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    FileType fileType = file.getFileType();
    System.out.println(fileType);
    // Get the document and project
    final Document document = editor.getDocument();

//    String[] prefix = new String[]{"chore","fix","build","style","feat"};
//    int max = 0;
//
//    if (prefix.length>0){
//      for (String s : prefix) {
//        max = Math.max(max,s.length());
//      }
//    }
//
//    if (max==0){
//      return Result.STOP;
//    }
//    int textLength = document.getTextLength();
//    if (textLength>max){
//      return Result.STOP;
//    }


    // Construct the runnable to substitute the string at offset 0 in the document
    Runnable runnable = () -> document.insertString(0, "editor_basics\n");
    // Make the document change in the context of a write action.
    WriteCommandAction.runWriteCommandAction(project, runnable);
    return Result.STOP;
  }

}
