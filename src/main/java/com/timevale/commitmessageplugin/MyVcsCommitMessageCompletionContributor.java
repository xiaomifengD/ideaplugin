package com.timevale.commitmessageplugin;

import com.timevale.commitmessageplugin.utils.CrmJacksonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.ui.CommitMessage;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.TextFieldWithAutoCompletionListProvider;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.ini4j.Ini;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author shanluo
 * @email shanluo@tsign.com
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
        System.out.println(parameters.getInvocationCount());
        Supplier<Iterable<Change>> changesSupplier = document.getUserData(CommitMessage.CHANGES_SUPPLIER_KEY);
        if (changesSupplier == null) {
            return;
        }

        result.stopHere();

        String text = parameters.getEditor().getDocument().getText();
        String prefix = TextFieldWithAutoCompletionListProvider.getCompletionPrefix(parameters);
        System.out.println(prefix);
        AtomicReference<String> prefixToMath = new AtomicReference<>();

        CompletionResultSet completionResultSet;
        if (Arrays.stream(BUG_INFO_SET).anyMatch(e -> {
            prefixToMath.set(e);
            return endWithIgnoreCase(prefix, e);
        })) {
            prefixToMath.set(prefixToMath.get());

            completionResultSet = result.withPrefixMatcher(new PlainPrefixMatcher(prefixToMath.get(), false));

            if (MyVcsCommitMessageCompletionContributor.GIT_USER == null) {
                return;
            }

            List<String> bugs =
                    queryMyBugs(MyVcsCommitMessageCompletionContributor.GIT_USER);

            if (bugs != null) {
                for (String bug : bugs) {
                    LookupElement lookupElement = LookupElementBuilder
                            .create(bug, bug)
                            .withLookupString(prefixToMath.get())
                            .withPsiElement(parameters.getPosition())
                            .withIcon(BUG_ICON)
                            .withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE);
                    completionResultSet.addElement(lookupElement);

                }
            }


            completionResultSet.restartCompletionOnPrefixChange(StandardPatterns.string().endsWith(":b"));


        } else if (text.length() < MAX_LENGTH) {
            completionResultSet = result.withPrefixMatcher(new PlainPrefixMatcher(prefix, true));
            List<LookupElement> lookupElements = Arrays.stream(ARR).map(s -> LookupElementBuilder
                    .create(s[0]).withTailText(s[1], true)
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

    private List<String> queryMyBugs(String user) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(1000)
                .setConnectionRequestTimeout(1000)
                .setSocketTimeout(1000)
                .build();


        HttpPost httpPost = new HttpPost("https://hua.esign.cn/proxy/dataDis/api/69/cs-soar-api-user-test");
        httpPost.addHeader("Content-Type", "application/json");
        String param = "{\"param\":[{\"name\":\"operatorId\",\"value\":\"${operator}\"}],\"token\":\"bb49d613-1c08-42c0-8590-d9b46785c393\"}"
                .replace("${operator}", user);
        httpPost.setEntity(new StringEntity(param, "UTF-8"));

        try (CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(requestConfig)
                .build()) {

            HttpResponse execute = httpClient.execute(httpPost);

            StatusLine statusLine = execute.getStatusLine();
            if (statusLine.getStatusCode() != 200) {
                return null;
            }
            String string = EntityUtils.toString(execute.getEntity(), "UTF-8");
            System.out.println(string);
            JsonNode jsonNode = CrmJacksonUtil.readTree(string);

            JsonNode code = jsonNode.get("code");
            if (code == null || code.asInt() != 0) {
                warn("请求BUG信息失败," + jsonNode.get("message").asText());
                return null;
            }

            JsonNode data = jsonNode.get("data");
            if (data == null) {
                return null;
            }

            List<String> list = new ArrayList<>();
            for (JsonNode jsonNode1 : data) {
                String name = jsonNode1.get("name").asText();
                String id = jsonNode1.get("id").asText();
                //name 最长取10个字符
                String bugStr = ":bug(" + id + ")" + name;
                list.add(bugStr);
            }
            return list;

        } catch (Exception e) {
            error("请求BUG信息失败," + e.getMessage());
        }
        return null;
    }

    private static void error(String message) {
        Notification notification =
                new Notification("commitMessagePlugin.balloon", message, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
    }


    private static void warn(String message) {
        Notification notification =
                new Notification("commitMessagePlugin.balloon", message, NotificationType.WARNING);
        Notifications.Bus.notify(notification);
    }

    private static void info(String message) {
        Notification notification =
                new Notification("commitMessagePlugin.balloon", message, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
    }


    static {
        try {

            Ini ini = new Ini(new FileInputStream(System.getProperty("user.home") + File.separator + ".gitconfig"));
            MyVcsCommitMessageCompletionContributor.GIT_USER = ini.get("user", "name");
            String userName = MyVcsCommitMessageCompletionContributor.GIT_USER;
            if (userName == null) {
                warn("未读取到git用户,请检查git配置文件.gitconfig是否在用户目录下");
            } else {
                info("读取到了当前git用户:" + MyVcsCommitMessageCompletionContributor.GIT_USER);
            }
        } catch (IOException e) {
            GIT_USER = null;
            error("读取git用户失败," + e.getMessage());
        }
    }


    private static String GIT_USER;

    private static final Icon BUG_ICON = IconLoader.getIcon("META-INF/bug.svg", MyVcsCommitMessageCompletionContributor.class);

    private static final String[][] ARR = new String[][]{
            {"chore: ", "构建过程或者辅助工具的新增/修改（如：webpack，gulp，package包升级等）"},
            {"fix: ", "问题（bug）修复"},
            {"build: ", "项目构建，或者依赖项构建"},
            {"style: ", "不影响代码含义的变化（空格，格式化，分号补全，lint格式相关问题解决等，不是修改css文件！)"},
            {"feat: ", "新功能（特性）增加"},
            {"test: ", "单元测试相关 新增/修改"},
            {"perf: ", "改进性能相关的代码提交"},
            {"docs: ", "文档相关 新增/修改"},
            {"refactor: ", "代码重构，未新增功能以及bug修复"},
            {"ci: ", "持续集成文件和脚本相关 新增/修改"}
    };

    private static final String[] BUG_INFO_SET = new String[]{":b", ":bu", ":bug"};

    private static final int MAX_LENGTH = Arrays.stream(ARR)
            .map(e -> e[0]).mapToInt(String::length).max().getAsInt();
}