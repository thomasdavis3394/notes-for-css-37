package org.dromara.easytl.parser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Source 类的测试
 *
 * @author EasyTL Team
 */
public class SourceTest {

    @Test
    public void testStringSource() throws IOException {
        String content = "Hello\nWorld";
        StringSource source = new StringSource(content);

        assertEquals('H', source.read());
        assertEquals(1, source.getLine());
        assertEquals(2, source.getColumn());

        assertEquals('e', source.read());
        assertEquals('l', source.read());
        assertEquals('l', source.read());
        assertEquals('o', source.read());

        assertEquals('\n', source.read());
        assertEquals(2, source.getLine());
        assertEquals(1, source.getColumn());

        assertEquals('W', source.read());
        assertEquals(2, source.getLine());
        assertEquals(2, source.getColumn());
    }

    @Test
    public void testStringSourcePeek() throws IOException {
        StringSource source = new StringSource("ABC");

        assertEquals('A', source.peek());
        assertEquals('A', source.peek());
        assertEquals('A', source.read());

        assertEquals('B', source.peek());
        assertEquals('C', source.peek(1));
        assertEquals(-1, source.peek(2));
    }

    @Test
    public void testStringSourceMarkAndReset() throws IOException {
        StringSource source = new StringSource("ABCDEF");

        source.read(); // A
        source.mark();
        assertEquals('B', source.read());
        assertEquals('C', source.read());
        source.reset();
        assertEquals('B', source.read());
    }

    @Test
    public void testStringSourceEOF() throws IOException {
        StringSource source = new StringSource("AB");

        assertFalse(source.isEOF());
        source.read(); // A
        source.read(); // B
        assertTrue(source.isEOF());
        assertEquals(-1, source.read());
    }

    @Test
    public void testStringSourceLineBreaks() throws IOException {
        // 测试 \n
        StringSource source1 = new StringSource("A\nB");
        source1.read(); // A
        source1.read(); // \n
        assertEquals(2, source1.getLine());

        // 测试 \r
        StringSource source2 = new StringSource("A\rB");
        source2.read(); // A
        source2.read(); // \r
        assertEquals(2, source2.getLine());

        // 测试 \r\n
        StringSource source3 = new StringSource("A\r\nB");
        source3.read(); // A
        source3.read(); // \r\n (读取为一个换行)
        assertEquals(2, source3.getLine());
        assertEquals('B', source3.peek());
    }

    @Test
    public void testStreamSource() throws IOException {
        String content = "Hello World";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        StreamSource source = new StreamSource(inputStream);

        assertEquals('H', source.read());
        assertEquals('e', source.read());
        assertEquals('l', source.read());
        assertEquals('l', source.read());
        assertEquals('o', source.read());
        assertEquals(' ', source.read());
        assertEquals('W', source.read());
    }

    @Test
    public void testStreamSourcePeek() throws IOException {
        String content = "ABC";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        StreamSource source = new StreamSource(inputStream);

        assertEquals('A', source.peek());
        assertEquals('A', source.peek());
        assertEquals('B', source.peek(1));
        assertEquals('C', source.peek(2));
        assertEquals('A', source.read());
    }

    @Test
    public void testStreamSourceMarkAndReset() throws IOException {
        String content = "ABCDEF";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        StreamSource source = new StreamSource(inputStream);

        source.read(); // A
        source.mark();
        assertEquals('B', source.read());
        assertEquals('C', source.read());
        source.reset();
        assertEquals('B', source.read());
    }

    /**
     * 测试 StringSource 处理超大字符串
     * StringSource 没有缓存限制，应该能正常处理任意大小的字符串
     */
    @Test
    public void testStringSourceLargeContent() throws IOException {
        // 生成一个 20000 字符的大字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20000; i++) {
            sb.append((char) ('A' + (i % 26)));
        }
        String largeContent = sb.toString();
        
        StringSource source = new StringSource(largeContent);
        
        // 测试能正常读取所有字符
        int count = 0;
        while (!source.isEOF()) {
            int ch = source.read();
            if (ch == -1) break;
            count++;
        }
        
        assertEquals(20000, count);
        assertTrue(source.isEOF());
    }

    /**
     * 测试 StringSource 在大字符串中的 mark/reset 功能
     */
    @Test
    public void testStringSourceLargeContentMarkAndReset() throws IOException {
        // 生成一个 15000 字符的大字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15000; i++) {
            sb.append((char) ('0' + (i % 10)));
        }
        String largeContent = sb.toString();
        
        StringSource source = new StringSource(largeContent);
        
        // 读取 5000 个字符
        for (int i = 0; i < 5000; i++) {
            source.read();
        }
        
        // 标记位置
        source.mark();
        int markedPosition = source.getPosition();
        
        // 继续读取 1000 个字符
        for (int i = 0; i < 1000; i++) {
            source.read();
        }
        
        // 重置并验证
        source.reset();
        assertEquals(markedPosition, source.getPosition());
    }

    /**
     * 测试 StreamSource 处理超过缓冲区大小的内容
     * StreamSource 默认缓冲区大小为 8192 字符
     */
    @Test
    public void testStreamSourceBufferBoundary() throws IOException {
        // 生成一个 20000 字符的大字符串，远超默认缓冲区大小 8192
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20000; i++) {
            sb.append((char) ('A' + (i % 26)));
        }
        String largeContent = sb.toString();
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
            largeContent.getBytes(StandardCharsets.UTF_8)
        );
        StreamSource source = new StreamSource(inputStream);
        
        // 逐个读取并验证字符
        for (int i = 0; i < 20000; i++) {
            int ch = source.read();
            assertEquals('A' + (i % 26), ch, "Position " + i + " mismatch");
        }
        
        assertTrue(source.isEOF());
        assertEquals(-1, source.read());
    }

    /**
     * 测试 StreamSource 在跨越缓冲区边界时的 peek 功能
     */
    @Test
    public void testStreamSourcePeekAcrossBuffer() throws IOException {
        // 生成一个 10000 字符的字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append((char) ('0' + (i % 10)));
        }
        String content = sb.toString();
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
            content.getBytes(StandardCharsets.UTF_8)
        );
        StreamSource source = new StreamSource(inputStream);
        
        // 读取到接近缓冲区边界的位置（8000 字符）
        for (int i = 0; i < 8000; i++) {
            source.read();
        }
        
        // 测试 peek 能否正确预览后续字符
        assertEquals('0' + (8000 % 10), source.peek());
        assertEquals('0' + (8001 % 10), source.peek(1));
        assertEquals('0' + (8010 % 10), source.peek(10));
        
        // 验证 peek 没有改变位置
        assertEquals('0' + (8000 % 10), source.read());
    }

    /**
     * 测试 StreamSource 跨越缓冲区的 mark/reset 功能
     * 注意：StreamSource 的 mark/reset 在 lookahead 范围内有效（默认 1024 字符）
     */
    @Test
    public void testStreamSourceMarkResetAcrossBuffer() throws IOException {
        // 生成一个 10000 字符的大字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append((char) ('A' + (i % 26)));
        }
        String content = sb.toString();
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
            content.getBytes(StandardCharsets.UTF_8)
        );
        StreamSource source = new StreamSource(inputStream);
        
        // 读取 8000 个字符（接近缓冲区边界）
        for (int i = 0; i < 8000; i++) {
            source.read();
        }
        
        // 标记当前位置
        source.mark();
        int markedLine = source.getLine();
        int markedColumn = source.getColumn();
        int markedPosition = source.getPosition();
        
        // 继续读取 500 个字符（在 lookahead 范围内）
        for (int i = 0; i < 500; i++) {
            source.read();
        }
        
        // 重置到标记位置
        source.reset();
        
        // 验证位置信息正确
        assertEquals(markedLine, source.getLine());
        assertEquals(markedColumn, source.getColumn());
        assertEquals(markedPosition, source.getPosition());
        
        // 验证能正确读取后续字符
        assertEquals('A' + (8000 % 26), source.read());
    }

    /**
     * 测试 StreamSource 使用自定义缓冲区大小
     */
    @Test
    public void testStreamSourceCustomBufferSize() throws IOException {
        // 生成一个 1000 字符的字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append((char) ('A' + (i % 26)));
        }
        String content = sb.toString();
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
            content.getBytes(StandardCharsets.UTF_8)
        );
        
        // 使用较小的缓冲区大小 (256)
        StreamSource source = new StreamSource(inputStream, StandardCharsets.UTF_8, 256);
        
        // 读取所有字符并验证
        for (int i = 0; i < 1000; i++) {
            int ch = source.read();
            assertEquals('A' + (i % 26), ch, "Position " + i + " mismatch");
        }
        
        assertTrue(source.isEOF());
    }

    /**
     * 测试 StreamSource 在缓冲区边界处理换行符
     */
    @Test
    public void testStreamSourceLineBreaksAcrossBuffer() throws IOException {
        // 生成一个包含换行符的大字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("Line ").append(i).append("\n");
        }
        String content = sb.toString();
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
            content.getBytes(StandardCharsets.UTF_8)
        );
        StreamSource source = new StreamSource(inputStream);
        
        // 读取所有内容，统计行数
        int lineCount = 1;
        while (!source.isEOF()) {
            int ch = source.read();
            if (ch == -1) break;
            if (ch == '\n') {
                lineCount++;
            }
        }
        
        // 验证行号正确（最后一行是空行，所以行号是 1001）
        assertEquals(1001, source.getLine());
    }

    /**
     * 测试 FileSource 读取超大模板文件
     * 该测试使用 test/resources/token_test.etl 文件
     */
    @Test
    public void testFileSourceLargeTemplateFile() throws IOException {
        // 获取测试资源文件路径
        String resourcePath = "src/test/resources/token_test.etl";
        java.io.File file = new java.io.File(resourcePath);
        
        // 确保文件存在
        assertTrue(file.exists(), "测试文件不存在: " + resourcePath);
        
        // 创建 FileSource
        FileSource source = new FileSource(file);
        
        // 读取所有字符并统计
        int charCount = 0;
        int lineCount = 1;
        int maxLineLength = 0;
        int currentLineLength = 0;
        
        while (!source.isEOF()) {
            int ch = source.read();
            if (ch == -1) break;
            
            charCount++;
            currentLineLength++;
            
            if (ch == '\n' || ch == '\r') {
                lineCount++;
                if (currentLineLength > maxLineLength) {
                    maxLineLength = currentLineLength;
                }
                currentLineLength = 0;
            }
        }
        
        // 验证读取的字符数 (超大文件应该有很多字符)
        assertTrue(charCount > 100000, "文件字符数应该超过 100000，实际: " + charCount);
        
        // 验证行数 (应该有很多行)
        assertTrue(lineCount > 5000, "文件行数应该超过 5000，实际: " + lineCount);
        
        // 验证已到达文件末尾
        assertTrue(source.isEOF());
        assertEquals(-1, source.read());
        
        System.out.println("FileSource 大文件测试统计:");
        System.out.println("  总字符数: " + charCount);
        System.out.println("  总行数: " + lineCount);
        System.out.println("  最长行长度: " + maxLineLength);
    }

    /**
     * 测试 FileSource 在超大文件中的 peek 功能
     */
    @Test
    public void testFileSourceLargeFilePeek() throws IOException {
        String resourcePath = "src/test/resources/token_test.etl";
        java.io.File file = new java.io.File(resourcePath);
        assertTrue(file.exists(), "测试文件不存在: " + resourcePath);
        
        FileSource source = new FileSource(file);
        
        // 测试文件开始处的 peek
        int firstChar = source.peek();
        assertEquals(firstChar, source.read());
        
        // 读取一定数量的字符
        for (int i = 0; i < 5000; i++) {
            source.read();
        }
        
        // 测试中间位置的 peek
        int peekChar = source.peek();
        int peekChar1 = source.peek(1);
        int peekChar5 = source.peek(5);
        
        // 验证 peek 不改变位置
        assertEquals(peekChar, source.read());
        assertEquals(peekChar1, source.read());
        
        // 继续读取
        source.read(); // skip 2
        source.read(); // skip 3
        source.read(); // skip 4
        assertEquals(peekChar5, source.read());
    }

    /**
     * 测试 FileSource 在超大文件中的 mark/reset 功能
     */
    @Test
    public void testFileSourceLargeFileMarkAndReset() throws IOException {
        String resourcePath = "src/test/resources/token_test.etl";
        java.io.File file = new java.io.File(resourcePath);
        assertTrue(file.exists(), "测试文件不存在: " + resourcePath);
        
        FileSource source = new FileSource(file);
        
        // 读取到某个位置
        for (int i = 0; i < 10000; i++) {
            source.read();
        }
        
        // 标记当前位置
        source.mark();
        int markedPosition = source.getPosition();
        int markedLine = source.getLine();
        int markedColumn = source.getColumn();
        
        // 读取一些字符
        char[] buffer = new char[100];
        for (int i = 0; i < 100; i++) {
            buffer[i] = (char) source.read();
        }
        
        // 重置到标记位置
        source.reset();
        
        // 验证位置信息恢复正确
        assertEquals(markedPosition, source.getPosition());
        assertEquals(markedLine, source.getLine());
        assertEquals(markedColumn, source.getColumn());
        
        // 验证重新读取的内容与之前相同
        for (int i = 0; i < 100; i++) {
            assertEquals(buffer[i], (char) source.read(), "Position " + i + " mismatch");
        }
    }

    /**
     * 测试 FileSource 读取超大文件并验证特定内容
     */
    @Test
    public void testFileSourceLargeFileContentVerification() throws IOException {
        String resourcePath = "src/test/resources/token_test.etl";
        java.io.File file = new java.io.File(resourcePath);
        assertTrue(file.exists(), "测试文件不存在: " + resourcePath);
        
        FileSource source = new FileSource(file);
        
        // 读取文件开头的内容
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            int ch = source.read();
            if (ch == -1) break;
            header.append((char) ch);
        }
        
        String headerStr = header.toString();
        
        // 验证文件开头包含标题注释
        assertTrue(headerStr.contains("EasyTL"), "文件开头应包含 'EasyTL'");
        assertTrue(headerStr.contains("token_test.etl"), "文件开头应包含 'token_test.etl'");
        
        // 继续读取，寻找特定的测试标记
        StringBuilder content = new StringBuilder();
        int batchMarkerCount = 0;
        String line = "";
        
        while (!source.isEOF()) {
            int ch = source.read();
            if (ch == -1) break;
            
            content.append((char) ch);
            line += (char) ch;
            
            if (ch == '\n' || ch == '\r') {
                // 检查是否包含批次标记
                if (line.contains("批次") && line.contains("测试用例")) {
                    batchMarkerCount++;
                }
                line = "";
            }
            
            // 只检查前面一部分内容以节省时间
            if (content.length() > 50000) {
                break;
            }
        }
        
        // 验证找到了批次测试标记
        assertTrue(batchMarkerCount > 0, "应该找到批次测试标记");
        
        System.out.println("找到批次测试标记数量: " + batchMarkerCount);
    }

    /**
     * 测试 FileSource 缓冲区跨越处理
     * 验证在读取超大文件时，缓冲区刷新不会影响数据完整性
     */
    @Test
    public void testFileSourceBufferBoundaryIntegrity() throws IOException {
        String resourcePath = "src/test/resources/token_test.etl";
        java.io.File file = new java.io.File(resourcePath);
        assertTrue(file.exists(), "测试文件不存在: " + resourcePath);
        
        FileSource source = new FileSource(file);
        
        // 读取多个缓冲区大小的数据（默认 8192）
        int bufferSize = 8192;
        int buffersToRead = 5;
        
        for (int buffer = 0; buffer < buffersToRead; buffer++) {
            int startPos = source.getPosition();
            
            // 在缓冲区边界附近测试 peek
            for (int i = 0; i < bufferSize - 10; i++) {
                source.read();
            }
            
            // 测试跨越缓冲区边界的 peek
            int[] peeked = new int[20];
            for (int i = 0; i < 20; i++) {
                peeked[i] = source.peek(i);
            }
            
            // 验证 peek 的结果与实际读取一致
            for (int i = 0; i < 20; i++) {
                int actual = source.read();
                assertEquals(peeked[i], actual, 
                    "缓冲区边界 peek 不一致，buffer=" + buffer + ", offset=" + i);
            }
        }
    }

    /**
     * 测试 FileSource 行号和列号追踪的准确性
     */
    @Test
    public void testFileSourceLineAndColumnTracking() throws IOException {
        String resourcePath = "src/test/resources/token_test.etl";
        java.io.File file = new java.io.File(resourcePath);
        assertTrue(file.exists(), "测试文件不存在: " + resourcePath);
        
        FileSource source = new FileSource(file);
        
        int currentLine = 1;
        int currentColumn = 1;
        int newlineCount = 0;
        
        // 读取前 1000 个字符，验证行列号
        for (int i = 0; i < 1000; i++) {
            assertEquals(currentLine, source.getLine(), "行号不匹配，位置: " + i);
            assertEquals(currentColumn, source.getColumn(), "列号不匹配，位置: " + i);
            
            int ch = source.read();
            if (ch == -1) break;
            
            if (ch == '\n' || ch == '\r') {
                currentLine++;
                currentColumn = 1;
                newlineCount++;
            } else {
                currentColumn++;
            }
        }
        
        assertTrue(newlineCount > 0, "应该遇到换行符");
        assertTrue(currentLine > 1, "应该跨越多行");
    }

    /**
     * 测试 FileSource 性能 - 读取速度测试
     */
    @Test
    public void testFileSourceReadingPerformance() throws IOException {
        String resourcePath = "src/test/resources/token_test.etl";
        java.io.File file = new java.io.File(resourcePath);
        assertTrue(file.exists(), "测试文件不存在: " + resourcePath);
        
        long startTime = System.currentTimeMillis();
        
        FileSource source = new FileSource(file);
        
        int charCount = 0;
        while (!source.isEOF()) {
            int ch = source.read();
            if (ch == -1) break;
            charCount++;
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("FileSource 性能测试:");
        System.out.println("  读取字符数: " + charCount);
        System.out.println("  耗时: " + duration + " ms");
        System.out.println("  速度: " + (charCount / (duration + 1)) + " 字符/ms");
        
        // 验证性能合理（读取超过 10万字符应该在合理时间内完成）
        assertTrue(duration < 5000, "读取时间不应超过 5 秒，实际: " + duration + " ms");
        assertTrue(charCount > 100000, "应该读取超过 10万字符");
    }
}

