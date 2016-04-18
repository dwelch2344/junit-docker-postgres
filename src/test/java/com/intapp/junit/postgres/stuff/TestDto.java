package com.intapp.junit.postgres.stuff;

public class TestDto {
    private int id;
    private String key;

    public TestDto(int id, String key) {
        this.id = id;
        this.key = key;
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestDto testDto = (TestDto) o;

        if (id != testDto.id) return false;
        return key.equals(testDto.key);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + key.hashCode();
        return result;
    }
}
