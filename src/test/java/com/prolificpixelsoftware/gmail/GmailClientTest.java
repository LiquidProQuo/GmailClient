package com.prolificpixelsoftware.gmail;

import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class GmailClientTest {

	private static final long ONE_DAY = 1000 * 60 * 60 * 24;

	/**
	 * NB: This is an integration test and will obviously fail if we haven't received any emails in 24 hours.
	 *
	 * @throws GeneralSecurityException - from Gmail Api
	 * @throws IOException - from Gmail Api
	 */
	@Test
	public void testGetTodaysEmailsFromGmailAsSimpleEmails() throws GeneralSecurityException, IOException {
		GmailClient gmailClient = new ReadOnlyGmailClient();

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis() - ONE_DAY);
		String formattedDateString = new SimpleDateFormat("yyyy/MM/dd").format(calendar.getTime());
		String query = "after:" + formattedDateString;

		List<SimpleEmail> emails =  SimpleEmail.fromMessages(gmailClient.getMessagesByQuery(query));
		assertNotNull(emails);
		assertFalse(emails.isEmpty());

		System.out.println("Num results: " + emails.size());
		for (SimpleEmail email : emails) {
			String subject = email.getSubject();
			Date date = email.getDate();
			String to = email.getTo();
			String from = email.getFrom();

			// NB: We allow for empty subjects and to's (can happen with Hangouts messages)
			assertNotNull(date);
			assertNotNull(from);

			System.out.println("Subject:" + subject);
			System.out.println("Date: " + date);
			System.out.println("To: " + to);
			System.out.println("From:" + from);
			System.out.println();
		}
	}

}