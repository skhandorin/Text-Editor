package editor;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextSearcher extends SwingWorker<Stream<MatchResult>, Object> {

    private String text;
    private String searchString;
    private boolean useRegex;
    private JTextArea textArea;

    private List<MatchResult> resultList;
    private int resultIndex = -1;

    public TextSearcher(String text, String searchString, boolean useRegex, JTextArea textArea) {
        this.text = text;
        this.searchString = searchString;
        this.useRegex = useRegex;
        this.textArea = textArea;
    }

    public String getText() {
        return text;
    }

    public String getSearchString() {
        return searchString;
    }

    public boolean isUseRegex() {
        return useRegex;
    }

    @Override
    protected Stream<MatchResult> doInBackground() throws Exception {
        int flags = Pattern.CASE_INSENSITIVE | (useRegex ? 0 : Pattern.LITERAL);
        Pattern searchPattern = Pattern.compile(searchString, flags);
        Matcher matcher = searchPattern.matcher(text);
        return matcher.results();
    }

    @Override
    protected void done() {
        try {
            resultList = get().collect(Collectors.toList());
            System.out.println("MaMatchResult: List contains " + resultList.size());

            if (resultList.isEmpty()) {
                System.out.println("MatchResult: resultList.isEmpty()");
                textArea.setCaretPosition(0);
                textArea.grabFocus();
                return;
            }

            resultIndex = 0;
            updateUI(0);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    protected void updateUI (int resultIndex) {
        MatchResult result = resultList.get(resultIndex);
        System.out.println(result.start() + " - " + result.end() + " - " + result.group());

        textArea.setCaretPosition(result.end());
        textArea.select(result.start(), result.end());
        textArea.grabFocus();
    }

    public void nextResult() {
        if (resultList == null || resultList.isEmpty()) {
            textArea.setCaretPosition(0);
            textArea.grabFocus();
            return;
        }

        resultIndex = (resultIndex + 1) % resultList.size();
        updateUI(resultIndex);
    }

    public void previousResult() {
        if (resultList == null || resultList.isEmpty()) {
            textArea.setCaretPosition(0);
            textArea.grabFocus();
            return;
        }

        resultIndex = (resultList.size() + resultIndex - 1) % resultList.size();
        updateUI(resultIndex);
    }
}
