package io.github.saveriobutright.jobradar.jobs;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobSearchCriteriaTest {

    @Test
    void normalizesFiltersAndCalculatesOffset() {
        JobSearchCriteria criteria = new JobSearchCriteria(
                3,
                20,
                "  data engineer  ",
                "   ",
                null
        );

        assertThat(criteria.page()).isEqualTo(3);
        assertThat(criteria.size()).isEqualTo(20);
        assertThat(criteria.query())
                .isEqualTo("data engineer");
        assertThat(criteria.location()).isNull();
        assertThat(criteria.remote()).isNull();
        assertThat(criteria.offset()).isEqualTo(40);
    }

    @Test
    void rejectsInvalidPagination() {
        assertThatThrownBy(() -> new JobSearchCriteria(
                0,
                20,
                null,
                null,
                null
        ))
                .isInstanceOf(InvalidSearchCriteriaException.class)
                .hasMessage("page must be at least 1");

        assertThatThrownBy(() -> new JobSearchCriteria(
                1,
                101,
                null,
                null,
                null
        ))
                .isInstanceOf(InvalidSearchCriteriaException.class)
                .hasMessage("size must be between 1 and 100");
    }
}
