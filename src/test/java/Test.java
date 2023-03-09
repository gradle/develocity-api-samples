import com.fasterxml.jackson.databind.ObjectMapper;
import demo.api.client.JSON;
import demo.api.model.AllPets;
import demo.api.model.Cat;
import demo.api.model.Dog;
import demo.api.model.Lizard;
import org.junit.jupiter.api.Assertions;

public class Test {
    ObjectMapper objectMapper = JSON.getDefault().getMapper();

    // -- here it works as the subtypes on Cat, Dog and Lizard only have the correct names and Jackson
    // Doesn't seem to care that the subtypes are actually wrong
    @org.junit.jupiter.api.Test
    public void cat() throws Exception {
        String catAsString = objectMapper.writeValueAsString(new Cat().name("Felix"));
        Assertions.assertEquals("{\"petType\":\"cat\",\"name\":\"Felix\"}", catAsString);
    }

    @org.junit.jupiter.api.Test
    public void dog() throws Exception {
        String catAsString = objectMapper.writeValueAsString(new Dog().bark("Woof"));
        Assertions.assertEquals("{\"petType\":\"dog\",\"bark\":\"Woof\"}", catAsString);
    }

    @org.junit.jupiter.api.Test
    public void lizard() throws Exception {
        String catAsString = objectMapper.writeValueAsString(new Lizard().lovesRocks(true));
        Assertions.assertEquals("{\"petType\":\"lizard\",\"lovesRocks\":true}", catAsString);
    }

    // here it fails as the subtypes now inspect the subtypes of Pet and this will see the upper-case names first
    // it will actually see resolve the correct names for the other pets by accident, because it collects subtypes
    // recursively, so it will see the lower-case names for Dog and Lizard on the Cat class.
    @org.junit.jupiter.api.Test
    public void all() throws Exception {
        String catAsString = objectMapper.writeValueAsString(new AllPets().addPetsItem(new Cat().name("Felix")));
        Assertions.assertEquals("{\"pets\":[{\"petType\":\"cat\",\"name\":\"Felix\"}]}", catAsString);
        //                                                         ^ it fails here as it writes "Cat" instead of "cat"
    }
}
