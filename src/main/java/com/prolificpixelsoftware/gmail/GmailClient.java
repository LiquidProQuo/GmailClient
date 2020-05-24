package com.prolificpixelsoftware.gmail;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple Gmail Client *currently* configured for Read-Only operations on caller's Gmail account.
 * Pre-Reqs: Leverages an existing Gmail App whose client_secret.json is placed in the resources folder.
 *
 * NB: On first run an auth link will be printed to console. Confirm to give your app account access with the
 * specified Scopes requested.
 * If these scopes have already been approved by this account, this step can be skipped by placing the
 * StoredCredential file in a folder labeled "credentials" in the root directory.
 *
 */
public class GmailClient {
	private static final String APPLICATION_NAME = "Gmail API Client";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String CREDENTIALS_FOLDER = "credentials"; // Directory to store user credentials.
	private static final String USER_SELF = "me";

	// Note the leading / is required when being referenced in a class not on the level of src/main/java
	private static final String CLIENT_SECRET_DIR = "/client_secret.json";

	private static Gmail internalService;

	/**
	 * Global instance of the scopes required by this quickstart.
	 * If modifying these scopes, delete your previously saved credentials/ folder.
	 */
	public GmailClient(String scope) throws GeneralSecurityException, IOException {
		initialize(Collections.singletonList(scope));
	}

	private void initialize(List<String> scopes) throws GeneralSecurityException, IOException {
		// Build a new authorized API client internalService.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		internalService = new Gmail.Builder(HTTP_TRANSPORT, JacksonFactory.getDefaultInstance(), getCredentials(HTTP_TRANSPORT, scopes))
				.setApplicationName(APPLICATION_NAME)
				.build();
	}

	/**
	 * Creates an authorized Credential object.
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If there is no client_secret.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, List<String> scopes) throws IOException {
		// Load client secrets.
		InputStream in = GmailClient.class.getResourceAsStream(CLIENT_SECRET_DIR);
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scopes)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(CREDENTIALS_FOLDER)))
				.setAccessType("offline")
				.build();
		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	}


	public List<Message> getMessagesByQuery(String query) throws IOException {
		return getMessagesByQuery(query, USER_SELF);
	}

	public List<Message> getMessagesByQuery(String query, String user) throws IOException {
		List<Message> fullMessages = new ArrayList<>();
		List<Message> partialMessages = listMessagesMatchingQuery(internalService, user, query);

		for (Message partialMessage : partialMessages) {
			Message fullMessage = internalService.users().messages().get(user, partialMessage.getId()).setFormat("raw").execute();
			fullMessages.add(fullMessage);
		}

		return fullMessages;
	}

	/**
	 * List all Messages of the user's mailbox matching the query.
	 *
	 * @param service Authorized Gmail API instance.
	 * @param userId User's email address. The special value "me"
	 * can be used to indicate the authenticated user.
	 * @param query String used to filter the Messages listed.
	 */
	private List<Message> listMessagesMatchingQuery(Gmail service, String userId,
														   String query) throws IOException {
		ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();

		List<Message> messages = new ArrayList<>();
		while (response.getMessages() != null) {
			messages.addAll(response.getMessages());
			if (response.getNextPageToken() == null) {
				break;
			}

			String pageToken = response.getNextPageToken();
			response = service.users().messages().list(userId).setQ(query).setPageToken(pageToken).execute();
		}

		return messages;
	}
}