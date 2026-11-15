package com.campusmate.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ValidatorsTest {

    @Test
    public void collegeDomainIsEnforcedWhenSet() {
        assertTrue(Validators.isAllowedEmail("asha@dsce.edu.in", "dsce.edu.in"));
        assertTrue(Validators.isAllowedEmail("ASHA@DSCE.EDU.IN", "dsce.edu.in"));
        assertFalse(Validators.isAllowedEmail("asha@gmail.com", "dsce.edu.in"));
        assertTrue(Validators.isAllowedEmail("asha@gmail.com", "")); // empty = any email
        assertEquals("Use your college email (@dsce.edu.in)", Validators.email("asha@gmail.com", "dsce.edu.in"));
    }

    @Test
    public void emailAndPasswordRules() {
        assertEquals("Enter your email", Validators.email("  ", ""));
        assertEquals("Enter a valid email", Validators.email("asha@", ""));
        assertNull(Validators.email("asha.rao@college.edu", ""));
        assertNotNull(Validators.password("12345"));
        assertNull(Validators.password("123456"));
        assertNotNull(Validators.confirmPassword("secret1", "secret2"));
    }

    @Test
    public void postAndClaimFieldRules() {
        assertNotNull(Validators.title("ab"));
        assertNull(Validators.title("Steel bottle"));
        assertNotNull(Validators.description("too short"));
        assertNull(Validators.description("Blue steel bottle with stickers"));
        assertEquals("End the question with \"?\"", Validators.proofQuestion("What colour is the sticker"));
        assertNull(Validators.proofQuestion("What colour is the sticker?"));
        assertNotNull(Validators.claimAnswer(" "));
        assertNotNull(Validators.chatMessage("   "));
        assertNull(Validators.chatMessage("Meet at the library?"));
    }
}
