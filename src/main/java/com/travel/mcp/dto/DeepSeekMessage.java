package com.travel.mcp.dto;

import java.util.List;

/**
 * DeepSeek 消息（兼容 OpenAI 格式）
 */
public class DeepSeekMessage {

    private String role;
    private String content;
    private String name;
    private String tool_call_id;
    private List<ToolCall> tool_calls;

    public DeepSeekMessage() {}

    public static DeepSeekMessage system(String content) {
        DeepSeekMessage msg = new DeepSeekMessage();
        msg.role = "system";
        msg.content = content;
        return msg;
    }

    public static DeepSeekMessage user(String content) {
        DeepSeekMessage msg = new DeepSeekMessage();
        msg.role = "user";
        msg.content = content;
        return msg;
    }

    public static DeepSeekMessage assistant(String content) {
        DeepSeekMessage msg = new DeepSeekMessage();
        msg.role = "assistant";
        msg.content = content;
        return msg;
    }

    public static DeepSeekMessage assistantWithToolCalls(List<ToolCall> toolCalls) {
        DeepSeekMessage msg = new DeepSeekMessage();
        msg.role = "assistant";
        msg.content = null;
        msg.tool_calls = toolCalls;
        return msg;
    }

    public static DeepSeekMessage toolResult(String toolCallId, String toolName, String result) {
        DeepSeekMessage msg = new DeepSeekMessage();
        msg.role = "tool";
        msg.tool_call_id = toolCallId;
        msg.name = toolName;
        msg.content = result;
        return msg;
    }

    // Getters & Setters
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTool_call_id() { return tool_call_id; }
    public void setTool_call_id(String tool_call_id) { this.tool_call_id = tool_call_id; }
    public List<ToolCall> getTool_calls() { return tool_calls; }
    public void setTool_calls(List<ToolCall> tool_calls) { this.tool_calls = tool_calls; }

    /**
     * 工具调用
     */
    public static class ToolCall {
        private String id;
        private String type;
        private Function function;

        public ToolCall() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Function getFunction() { return function; }
        public void setFunction(Function function) { this.function = function; }

        public static class Function {
            private String name;
            private String arguments;

            public Function() {}

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public String getArguments() { return arguments; }
            public void setArguments(String arguments) { this.arguments = arguments; }
        }
    }
}
