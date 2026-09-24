/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.mail.common.impl.jakarta.mail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import org.junit.jupiter.api.Test;

class JakartaMailFlowableMailClientTest {

    protected final JakartaMailFlowableMailClient client = new JakartaMailFlowableMailClient(null, null);

    @Test
    void attachmentFileNameThatIsLongAndNonAsciiIsNotCorrupted() throws MessagingException {
        String fileName = "Précis of Evidence VA23-5-1034 Lorge Chocolatier PN 1136882 Retail Henry Street Kenmare Co.pdf";

        ByteArrayDataSource attachment = new ByteArrayDataSource("attachment content".getBytes(), "application/pdf");
        attachment.setName(fileName);

        MimeMultipart multipart = client.createMultiPartContent(null, null, "UTF-8", List.of(attachment));

        BodyPart attachmentPart = multipart.getBodyPart(0);
        assertThat(attachmentPart.getFileName()).isEqualTo(fileName);
    }

    @Test
    void attachmentFileNameThatIsShortAndAsciiIsUnaffected() throws MessagingException {
        String fileName = "invoice.pdf";
        ByteArrayDataSource attachment = new ByteArrayDataSource("attachment content".getBytes(), "application/pdf");
        attachment.setName(fileName);

        MimeMultipart multipart = client.createMultiPartContent(null, null, "UTF-8", List.of(attachment));

        BodyPart attachmentPart = multipart.getBodyPart(0);
        assertThat(attachmentPart.getFileName()).isEqualTo(fileName);
    }

    @Test
    void attachmentFileNameIsEncodedWithTheGivenCharsetRatherThanThePlatformDefault() throws MessagingException {
        String fileName = "café.pdf";
        ByteArrayDataSource attachment = new ByteArrayDataSource("attachment content".getBytes(), "application/pdf");
        attachment.setName(fileName);

        MimeMultipart multipart = client.createMultiPartContent(null, null, "ISO-8859-1", List.of(attachment));

        BodyPart attachmentPart = multipart.getBodyPart(0);
        String contentDisposition = attachmentPart.getHeader("Content-Disposition")[0];

        // 'é' is a single byte (0xE9) in ISO-8859-1 but two bytes (0xC3 0xA9) in UTF-8: this string can only
        // appear if the ISO-8859-1 charset passed to createMultiPartContent() was actually used to encode it,
        // rather than UTF-8 or the JVM's platform default charset.
        assertThat(contentDisposition).contains("ISO-8859-1''caf%E9.pdf");
        assertThat(attachmentPart.getFileName()).isEqualTo(fileName);
    }

}
