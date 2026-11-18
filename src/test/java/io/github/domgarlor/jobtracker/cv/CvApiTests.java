package io.github.domgarlor.jobtracker.cv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import io.github.domgarlor.jobtracker.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class CvApiTests {

	@TempDir
	static Path storageDir;

	@DynamicPropertySource
	static void storageProperties(DynamicPropertyRegistry registry) {
		registry.add("app.storage.cv-dir", () -> storageDir.toString());
	}

	@Autowired
	MockMvc mvc;

	@Test
	void createsAProfile() throws Exception {
		String name = uniqueName("QA");

		mvc.perform(post("/api/cv-profiles").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"%s\",\"description\":\"QA automation\"}".formatted(name)))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.name").value(name))
			.andExpect(jsonPath("$.description").value("QA automation"));
	}

	@Test
	void rejectsADuplicateProfileNameIgnoringCase() throws Exception {
		String name = uniqueName("Backend .NET");
		createProfile(name);

		mvc.perform(post("/api/cv-profiles").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"%s\"}".formatted(name.toUpperCase())))
			.andExpect(status().isConflict());
	}

	@Test
	void rejectsAProfileWithoutName() throws Exception {
		mvc.perform(post("/api/cv-profiles").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" \"}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void listsProfiles() throws Exception {
		String name = uniqueName("BBDD");
		createProfile(name);

		mvc.perform(get("/api/cv-profiles"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.name == '%s')]".formatted(name)).exists());
	}

	@Test
	void uploadsAPdfVersionAndStoresItsHash() throws Exception {
		long profileId = createProfile(uniqueName("QA"));
		byte[] pdf = uniquePdf();

		mvc.perform(multipart("/api/cv-profiles/{id}/versions", profileId)
				.file(new MockMultipartFile("file", "curriculum_QA.pdf", "application/pdf", pdf))
				.param("notes", "Added Xray"))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andExpect(jsonPath("$.profileId").value(profileId))
			.andExpect(jsonPath("$.originalFilename").value("curriculum_QA.pdf"))
			.andExpect(jsonPath("$.sizeBytes").value(pdf.length))
			.andExpect(jsonPath("$.sha256").value(sha256(pdf)))
			.andExpect(jsonPath("$.notes").value("Added Xray"))
			.andExpect(jsonPath("$.uploadedAt").exists());

		assertThat(storageDir.resolve(sha256(pdf) + ".pdf")).hasBinaryContent(pdf);
	}

	@Test
	void rejectsAFileThatIsNotAPdf() throws Exception {
		long profileId = createProfile(uniqueName("QA"));

		mvc.perform(multipart("/api/cv-profiles/{id}/versions", profileId)
				.file(new MockMultipartFile("file", "cv.pdf", "application/pdf", "plain text".getBytes())))
			.andExpect(status().isUnsupportedMediaType());
	}

	@Test
	void rejectsAnIdenticalPdfAndPointsToTheExistingVersion() throws Exception {
		byte[] pdf = uniquePdf();
		long existingId = uploadVersion(createProfile(uniqueName("QA")), pdf);
		long otherProfileId = createProfile(uniqueName("Backend .NET-Java"));

		mvc.perform(multipart("/api/cv-profiles/{id}/versions", otherProfileId)
				.file(new MockMultipartFile("file", "copy.pdf", "application/pdf", pdf)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.existingVersionId").value(existingId));
	}

	@Test
	void returnsNotFoundWhenUploadingToAnUnknownProfile() throws Exception {
		mvc.perform(multipart("/api/cv-profiles/{id}/versions", 999_999)
				.file(new MockMultipartFile("file", "cv.pdf", "application/pdf", uniquePdf())))
			.andExpect(status().isNotFound());
	}

	@Test
	void listsTheVersionsOfAProfileNewestFirst() throws Exception {
		long profileId = createProfile(uniqueName("QA"));
		long first = uploadVersion(profileId, uniquePdf());
		long second = uploadVersion(profileId, uniquePdf());

		mvc.perform(get("/api/cv-profiles/{id}/versions", profileId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].id").value(second))
			.andExpect(jsonPath("$[1].id").value(first));
	}

	@Test
	void downloadsTheExactPdfThatWasUploaded() throws Exception {
		byte[] pdf = uniquePdf();
		long versionId = uploadVersion(createProfile(uniqueName("QA")), pdf);

		mvc.perform(get("/api/cv-versions/{id}/file", versionId))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_PDF))
			.andExpect(header().string("Content-Disposition", startsWith("attachment; filename=\"cv.pdf\"")))
			.andExpect(content().bytes(pdf));
	}

	@Test
	void versionsCannotBeModifiedOrDeleted() throws Exception {
		long versionId = uploadVersion(createProfile(uniqueName("QA")), uniquePdf());

		mvc.perform(put("/api/cv-versions/{id}", versionId).contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isMethodNotAllowed());
		mvc.perform(delete("/api/cv-versions/{id}", versionId)).andExpect(status().isMethodNotAllowed());
		mvc.perform(get("/api/cv-versions/{id}", versionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(versionId));
	}

	private long createProfile(String name) throws Exception {
		MvcResult result = mvc
			.perform(post("/api/cv-profiles").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"%s\"}".formatted(name)))
			.andExpect(status().isCreated())
			.andReturn();
		return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
	}

	private long uploadVersion(long profileId, byte[] pdf) throws Exception {
		MvcResult result = mvc
			.perform(multipart("/api/cv-profiles/{id}/versions", profileId)
				.file(new MockMultipartFile("file", "cv.pdf", "application/pdf", pdf)))
			.andExpect(status().isCreated())
			.andReturn();
		return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
	}

	private static String uniqueName(String prefix) {
		return prefix + " " + UUID.randomUUID();
	}

	private static byte[] uniquePdf() {
		return ("%PDF-1.7\n% " + UUID.randomUUID() + "\n%%EOF\n").getBytes(StandardCharsets.US_ASCII);
	}

	private static String sha256(byte[] bytes) throws Exception {
		return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
	}

}
