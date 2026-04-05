package com.example.allot.controller.organizer;

import static org.junit.Assert.assertEquals;

import com.example.allot.model.organizer.EntrantExportRow;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

public class EventEntrantsCsvFormatterTest {
    private EventEntrantsCsvFormatter formatter;

    @Before
    public void setUp() {
        formatter = new EventEntrantsCsvFormatter();
    }

    @Test
    public void format_returnsHeaderOnlyWhenRowsAreEmpty() {
        assertEquals("Name,Email,Phone", formatter.format(Collections.emptyList()));
    }

    @Test
    public void format_writesSingleNormalRowWithoutQuoting() {
        String csv = formatter.format(Collections.singletonList(
                new EntrantExportRow("Jane Doe", "jane@example.com", "555-1111")
        ));

        assertEquals("Name,Email,Phone\nJane Doe,jane@example.com,555-1111", csv);
    }

    @Test
    public void format_quotesValuesContainingCommas() {
        String csv = formatter.format(Collections.singletonList(
                new EntrantExportRow("Doe, Jane", "jane@example.com", "555-1111")
        ));

        assertEquals("Name,Email,Phone\n\"Doe, Jane\",jane@example.com,555-1111", csv);
    }

    @Test
    public void format_escapesEmbeddedQuotes() {
        String csv = formatter.format(Collections.singletonList(
                new EntrantExportRow("Jane \"JJ\" Doe", "jane@example.com", "555-1111")
        ));

        assertEquals("Name,Email,Phone\n\"Jane \"\"JJ\"\" Doe\",jane@example.com,555-1111", csv);
    }

    @Test
    public void format_quotesMultilineValues() {
        String csv = formatter.format(Collections.singletonList(
                new EntrantExportRow("Jane Doe", "jane@example.com", "Line 1\nLine 2")
        ));

        assertEquals("Name,Email,Phone\nJane Doe,jane@example.com,\"Line 1\nLine 2\"", csv);
    }

    @Test
    public void format_preservesBlankEmailAndPhoneCells() {
        String csv = formatter.format(Collections.singletonList(
                new EntrantExportRow("Jane Doe", "", null)
        ));

        assertEquals("Name,Email,Phone\nJane Doe,,", csv);
    }

    @Test
    public void format_preservesInputOrder() {
        String csv = formatter.format(Arrays.asList(
                new EntrantExportRow("First Person", "first@example.com", "111"),
                new EntrantExportRow("Second Person", "second@example.com", "222")
        ));

        assertEquals(
                "Name,Email,Phone\nFirst Person,first@example.com,111\nSecond Person,second@example.com,222",
                csv
        );
    }
}
