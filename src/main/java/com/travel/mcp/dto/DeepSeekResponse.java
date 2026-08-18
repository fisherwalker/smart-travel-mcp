package com.travel.mcp.dto;

import java.util.List;

/**
 * DeepSeek API 响应（兼容 OpenAI 格式）
 */
public class DeepSeekResponse {

    private String id;
    private List<Choice> choices;

    public DeepSeekResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public List<Choice> getChoices() { return choices; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }

    public static class Choice {
        private int index;
        private DeepSeekMessage message;
        private Delta delta;
        private String finish_reason;

        public Choice() {}

        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        public DeepSeekMessage getMessage() { return message; }
        public void setMessage(DeepSeekMessage message) { this.message = message; }
        public Delta getDelta() { return delta; }
        public void setDelta(Delta delta) { this.delta = delta; }
        public String getFinish_reason() { return finish_reason; }
        public void setFinish_reason(String finish_reason) { this.finish_reason = finish_reason; }
    }

    public static class Delta {
        private String role;
        private String content;
        private List<DeepSeekMessage.ToolCall> tool_calls;

        public Delta() {}

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public List<DeepSeekMessage.ToolCall> getTool_calls() { return tool_calls; }
        public void setTool_calls(List<DeepSeekMessage.ToolCall> tool_calls) { this.tool_calls = tool_calls; }
    }
}
