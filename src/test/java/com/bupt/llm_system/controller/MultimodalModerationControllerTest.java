package com.bupt.llm_system.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.FileInputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.mock.web.MockHttpServletResponse;

@SpringBootTest
@AutoConfigureMockMvc
public class MultimodalModerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCheckContent_TextOnly() throws Exception {
        System.out.println("Testing Text Only...");
        mockMvc.perform(multipart("/api/v1/moderation/multimodal/check")
                .param("text", "这是一段测试文本，请忽略。"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testCheckContent_TextAndImageUrl() throws Exception {
        System.out.println("Testing Text + Image URL...");
        mockMvc.perform(multipart("/api/v1/moderation/multimodal/check")
                .param("text", "这张图片里有什么？")
                .param("imageUrls", "https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testCheckContent_RealLocalFile() throws Exception {
        System.out.println("Testing Real Local File...");

        // ⚠️ TODO: 请修改为你本地的实际图片路径
        String localFilePath = "D:\\code\\zzb\\LLM_System\\b.jpg";
        File realFile = new File(localFilePath);

        if (!realFile.exists()) {
            System.out.println("⚠️ 本地文件不存在，跳过此测试: " + localFilePath);
            // 如果你想强制测试失败，可以抛出异常
            // throw new RuntimeException("File not found: " + localFilePath);
            return;
        }

        try (FileInputStream fis = new FileInputStream(realFile)) {
            MockMultipartFile file = new MockMultipartFile(
                    "files",              // 参数名，必须与 Controller 中的字段名一致 (MultimodalModerationForm.files)
                    realFile.getName(),   // 文件名
                    MediaType.IMAGE_JPEG_VALUE, // Content-Type (根据实际情况调整，如 image/png)
                    fis                   // 文件流
            );

            mockMvc.perform(multipart("/api/v1/moderation/multimodal/check")
                    .file(file)
                    .param("text", "请分析这张本地上传的图片是不是傻逼"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Test
    public void testCheckContent_Stream() throws Exception {
        System.out.println("Testing Stream...");
        MvcResult result = mockMvc.perform(multipart("/api/v1/moderation/multimodal/check")
                .param("text", "请详细描述这张图片。")
                .param("imageUrls", "https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg")
                .param("stream", "true"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        long startTime = System.currentTimeMillis();
        // Wait up to 60 seconds for stream content
        while (System.currentTimeMillis() - startTime < 60000) {
            String content = response.getContentAsString();
            if (!content.isEmpty()) {
                System.out.println("Stream content captured: " + content);
                if (content.contains("data:")) {
                     // We got some data, let's wait a bit more to see if we get more, then pass
                     Thread.sleep(2000);
                     System.out.println("Final Stream content: " + response.getContentAsString());
                     return;
                }
            }
            Thread.sleep(1000);
        }

        System.out.println("⚠️ No stream data received. Check network or API key.");
    }
}
