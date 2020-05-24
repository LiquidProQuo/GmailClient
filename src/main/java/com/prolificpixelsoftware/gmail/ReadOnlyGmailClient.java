package com.prolificpixelsoftware.gmail;

import com.google.api.services.gmail.GmailScopes;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Simple Gmail Client for R/O use cases. Follows same rules as base GmailClient class with added limitation that
 * "https://www.googleapis.com/auth/gmail.readonly" is the only permitted scope.
 *
 * Created by Joshua Speight on 5/24/2020
 */
public class ReadOnlyGmailClient extends GmailClient{
	public ReadOnlyGmailClient() throws GeneralSecurityException, IOException {
		super(GmailScopes.GMAIL_READONLY);
	}
}
