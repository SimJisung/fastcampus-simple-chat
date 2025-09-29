package ai.dawn.simple_chat.rag;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.util.StringUtils;

public class LengthTextSplitter extends TextSplitter {

    private final int chunkSize;

    private final int chunkOverlap;

    public LengthTextSplitter(int chunkSize, int chunkOverlap) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }


    @Override
    public List<String> splitText(String text) {
        List<String> chunks = new ArrayList<>();

        if (!StringUtils.hasText(text)) {
            return chunks;
        }

        if (text.length() <= chunkOverlap) {
            chunks.add(text);
            return chunks;
        }

        int position = 0;
        while (position < text.length()) {
            int end = Math.min(position + chunkSize, text.length());
            chunks.add(text.substring(position, end));
            int nextPosition = end - chunkOverlap;
            if (nextPosition <= position) {
                break;
            }
            position = nextPosition;
        }

        return chunks;
    }

}
