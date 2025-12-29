package com.bupt.llm_system.service.impl;

import com.bupt.llm_system.service.IMultimodalModerationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class MultimodalModerationService implements IMultimodalModerationService {

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${llm.base-url}")
    private String baseUrl;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public String checkContent(String text, List<String> images) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", "qwen-vl-plus"); // Using qwen-vl-plus as standard. If qwen3-vl-plus is available, change here.

            ArrayNode messages = body.putArray("messages");

            // System Prompt
            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", """
你是一个多模态内容安全审核助手。请分析提供的文本和图片，判断是否存在违规内容。
请识别以下类别，用 0 或 1 表示：

porn 涉黄
politics 涉政
illegal 违禁
terrorism 暴恐
abusive 谩骂
advertising 广告
party_gov_leaders 涉及党政机关及领导人
hmt_sovereignty 涉及港澳台与领土主权
laws_regulation 涉及法律法规
ethnic_religion 涉及民族宗教
national_security 涉及国家安全与核心机密
international_relations 涉及国际关系
current_affairs 涉及时政与社会生活
sensitive_people_events 涉及敏感人物与事件
privacy_leak 涉及个人隐私泄露
historical_nihilism_fake 涉及历史虚无主义与造谣

此外，请提供一段简短的解读内容，解释判断理由。

输出格式：
{
  "labels": { ... },
  "comment": "解读内容"
}
""");

            // User Message
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            ArrayNode contentArray = userMessage.putArray("content");

            // Add images
            if (images != null) {
                for (String imgUrl : images) {
                    ObjectNode imgNode = contentArray.addObject();
                    imgNode.put("type", "image_url");
                    imgNode.putObject("image_url").put("url", imgUrl);
                }
            }

            // Add text
            if (text != null && !text.isEmpty()) {
                ObjectNode textNode = contentArray.addObject();
                textNode.put("type", "text");
                textNode.put("text", text);
            }

            Request request = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .post(RequestBody.create(
                            body.toString(),
                            MediaType.parse("application/json")
                    ))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("API call failed: " + response.code() + " " + response.message());
                }
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new RuntimeException("API response body is null");
                }
                String json = responseBody.string();
                return mapper.readTree(json)
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();
            }

        } catch (Exception e) {
            throw new RuntimeException("多模态审核失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void checkContentStream(String text, List<String> images, SseEmitter emitter) {
        executorService.execute(() -> {
            try {
                ObjectNode body = mapper.createObjectNode();
                body.put("model", "qwen-vl-plus");
                body.put("stream", true);

                ArrayNode messages = body.putArray("messages");

                // System Prompt
                ObjectNode systemMessage = messages.addObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", """
你是一个多模态内容安全审核助手。请分析提供的文本和图片，判断是否存在违规内容。
请识别以下类别，用 0 或 1 表示：

porn 涉黄
politics 涉政
illegal 违禁
terrorism 暴恐
abusive 谩骂
advertising 广告
party_gov_leaders 涉及党政机关及领导人
hmt_sovereignty 涉及港澳台与领土主权
laws_regulation 涉及法律法规
ethnic_religion 涉及民族宗教
national_security 涉及国家安全与核心机密
international_relations 涉及国际关系
current_affairs 涉及时政与社会生活
sensitive_people_events 涉及敏感人物与事件
privacy_leak 涉及个人隐私泄露
historical_nihilism_fake 涉及历史虚无主义与造谣

此外，请提供一段简短的解读内容，解释判断理由。

输出格式：
{
  "labels": { ... },
  "comment": "解读内容"
}
""");

                // User Message
                ObjectNode userMessage = messages.addObject();
                userMessage.put("role", "user");
                ArrayNode contentArray = userMessage.putArray("content");

                // Add images
                if (images != null) {
                    for (String imgUrl : images) {
                        ObjectNode imgNode = contentArray.addObject();
                        imgNode.put("type", "image_url");
                        imgNode.putObject("image_url").put("url", imgUrl);
                    }
                }

                // Add text
                if (text != null && !text.isEmpty()) {
                    ObjectNode textNode = contentArray.addObject();
                    textNode.put("type", "text");
                    textNode.put("text", text);
                }

                Request request = new Request.Builder()
                        .url(baseUrl + "/chat/completions")
                        .post(RequestBody.create(
                                body.toString(),
                                MediaType.parse("application/json")
                        ))
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .addHeader("Accept", "text/event-stream")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        emitter.send(SseEmitter.event().name("error").data("API call failed: " + response.code()));
                        emitter.complete();
                        return;
                    }

                    ResponseBody responseBody = response.body();
                    if (responseBody == null) {
                        emitter.send(SseEmitter.event().name("error").data("API response body is null"));
                        emitter.complete();
                        return;
                    }

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                if ("[DONE]".equals(data)) {
                                    break;
                                }
                                try {
                                    JsonNode node = mapper.readTree(data);
                                    JsonNode choices = node.path("choices");
                                    if (choices.isArray() && !choices.isEmpty()) {
                                        JsonNode delta = choices.get(0).path("delta");
                                        if (delta.has("content")) {
                                            String content = delta.get("content").asText();
                                            emitter.send(content);
                                        }
                                    }
                                } catch (Exception e) {
                                    // Ignore parse errors for individual chunks
                                }
                            }
                        }
                    }
                    emitter.complete();
                }
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data("Stream failed: " + e.getMessage()));
                } catch (Exception ex) {
                    // Ignore
                }
                emitter.completeWithError(e);
            }
        });
    }
}
