package com.prolificpixelsoftware.gmail;

import com.google.api.client.util.Base64;
import com.google.api.client.util.StringUtils;
import com.google.api.services.gmail.model.Message;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class representing an Email object with only very common elements exposed for most common use cases.
 * NB: Slightly coupled with GmailClient and com.google.api.services.gmail.model.Messages.
 *
 * Created by Joshua Speight on 5/24/2020
 */
public class SimpleEmail {
	private static final String FROM_KEY = "From:";
	private static final String TO_KEY = "To:";
	private static final String SUBJECT_KEY = "Subject:";

	private Date date;
	private String content;
	private String subject;
	private String to;
	private String from;

	public SimpleEmail() {}

	public Date getDate() {
		return date;
	}
	public String getContent() {
		return content;
	}
	public String getSubject() {
		return subject;
	}
	public String getTo() {
		return to;
	}
	public String getFrom() {
		return from;
	}

	private void setDate(Date date) {
		this.date = date;
	}
	private void setContent(String content) {
		this.content = content;
	}
	private void setSubject(String subject) {
		this.subject = subject;
	}
	private void setTo(String to) {
		this.to = to;
	}
	private void setFrom(String from) {
		this.from = from;
	}

	private static SimpleEmail createSimpleEmailFromContent(Date date, String content) {
		SimpleEmail simpleEmail = new SimpleEmail();
		simpleEmail.setDate(date);
		simpleEmail.setContent(content);

		List<String> lines = new BufferedReader(new StringReader(content)).lines().collect(Collectors.toList());
		for (String line : lines) {
			if (simpleEmail.isPopulated()) {
				return simpleEmail;
			}
			if (line.startsWith(FROM_KEY)) {
				simpleEmail.setFrom(line.substring(FROM_KEY.length()).trim());
			}
			if (line.startsWith(TO_KEY)) {
				simpleEmail.setTo(line.substring(TO_KEY.length()).trim());
			}
			if (line.startsWith(SUBJECT_KEY)) {
				simpleEmail.setSubject(line.substring(SUBJECT_KEY.length()).trim());
			}
		}
		return simpleEmail;
	}

	private boolean isPopulated() {
		return subject != null && to != null && from != null;
	}

	public static SimpleEmail fromMessage(Message message) {
		String encodedData = message.getRaw();
		byte[] decodedData = Base64.decodeBase64(encodedData);
		String content = StringUtils.newStringUtf8(decodedData);
		Date date = new Date(message.getInternalDate());
		return createSimpleEmailFromContent(date, content);
	}

	public static List<SimpleEmail> fromMessages(List<Message> messages) {
		if (messages == null || messages.isEmpty()) {
			return Collections.emptyList();
		}
		return messages.stream().map(SimpleEmail::fromMessage).collect(Collectors.toList());
	}
}