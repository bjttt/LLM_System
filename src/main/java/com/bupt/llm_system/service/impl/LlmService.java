package com.bupt.llm_system.service.impl;

import com.bupt.llm_system.pojo.dto.ResultCode;
import com.bupt.llm_system.service.ILlmService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LlmService implements ILlmService {

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${llm.base-url}")
    private String baseUrl;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String ask(String question) {

        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", "qwen-plus");

            ArrayNode messages = body.putArray("messages");
            messages.addObject()
                    .put("role", "user")
                    .put("content", question);

            Request request = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .post(RequestBody.create(
                            body.toString(),
                            MediaType.parse("application/json")
                    ))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            Response response = client.newCall(request).execute();
            String json = response.body().string();

            return mapper.readTree(json)
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {
            throw new RuntimeException("大模型调用失败");
        }
    }

    @Override
public String classifyText(String text) {

    try {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", "qwen-plus");

        ArrayNode messages = body.putArray("messages");

        messages.addObject()
                .put("role", "system")
                .put("content", """
你是一个文本风险分类器。只输出 JSON，不要解释。

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

输出格式：
{ "labels": { ... } }
""");

        messages.addObject()
                .put("role", "user")
                .put("content", text);

        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .post(RequestBody.create(
                        body.toString(),
                        MediaType.parse("application/json")
                ))
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        Response response = client.newCall(request).execute();
        String json = response.body().string();

        return mapper.readTree(json)
                .path("choices").get(0)
                .path("message").path("content")
                .asText();

    } catch (Exception e) {
        throw new RuntimeException("分类模型调用失败", e);
    }
}

}
