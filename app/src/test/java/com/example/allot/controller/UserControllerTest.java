package com.example.allot.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

public class UserControllerTest {

    private UserController userController;

    @Before
    public void setUp() {
    }

    @Test
    public void testNormalizePhone_RemovesExtraSpaces() throws Exception {
        Method normalizePhone = UserController.class.getDeclaredMethod("normalizePhone", String.class);
        normalizePhone.setAccessible(true);

        String rawPhone = "  1234567890  ";
        String expected = "1234567890";
        String result = (String) normalizePhone.invoke(null, rawPhone);
        assertEquals(expected, result);
    }

    @Test
    public void testIsBlank_ReturnsTrueForEmptyStrings() throws Exception {
        Method isBlank = UserController.class.getDeclaredMethod("isBlank", String.class);
        isBlank.setAccessible(true);

        assertTrue((Boolean) isBlank.invoke(null, ""));
        assertTrue((Boolean) isBlank.invoke(null, "   "));
        assertTrue((Boolean) isBlank.invoke(null, (String) null));
        assertFalse((Boolean) isBlank.invoke(null, "Valid Text"));
    }
}