package com.mycompany.tiketwahana.core;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class MigrationsTest {
    @Test void splitterKeepsSemicolonsInsideStringsAndDropsComments() {
        List<String> sql = Migrations.split("-- comment;\nINSERT INTO x VALUES ('a;b'); /* hi; */ SELECT 1;");
        assertEquals(2, sql.size());
        assertTrue(sql.get(0).contains("'a;b'"));
        assertEquals("SELECT 1", sql.get(1));
    }
}
