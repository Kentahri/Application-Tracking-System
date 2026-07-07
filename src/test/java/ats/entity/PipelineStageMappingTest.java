package ats.entity;

import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.hibernate.annotations.SQLDelete;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PipelineStageMappingTest {

    @Test
    void mapsToTableReferencedByStageTransitionForeignKeys() {
        Table table = PipelineStage.class.getAnnotation(Table.class);
        SQLDelete sqlDelete = PipelineStage.class.getAnnotation(SQLDelete.class);

        assertEquals("pipeline_stages", table.name());
        assertEquals(
                "UPDATE pipeline_stages SET is_deleted = 1, update_at = CURRENT_TIMESTAMP, deleted_at = CURRENT_TIMESTAMP WHERE id = ? and is_deleted = 0",
                sqlDelete.sql()
        );
    }
}
