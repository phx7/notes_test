import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import java.util.*;
import java.util.stream.Collectors;
import static spark.Spark.*;

public class Main {
    private static List<Note> data = new ArrayList<>();
    public static void main(String[] args) {
        data.add(new Note("random text", "header"));
        data.add(new Note("random text 2", "header 2"));

        port(80);
        get("/notes", (req,res) -> {
            Map<String, Object> model = new HashMap<>();
            if (!req.session().attributes().isEmpty()) {
                model.put("error", req.session().attribute("error"));
                req.session().removeAttribute("error");
            }
            model.put("data", data);
            return render(model);
        });
        post("/notes", (req,res) -> {
            if(req.queryParams("text").length() == 0) {
                req.session().attribute("error","Необходимо ввести текст заметки");
            } else {
                data.add(new Note(req.queryParams("text"), req.queryParams("header")));
            }
            res.redirect("/notes");
            return false;
        });
        get("/notes/remove/:id", (req,res) -> {
            data.remove(Integer.parseInt(req.params(":id")) - 1);
            res.redirect("/notes");
            return true;
        });
        get("/notes/search/", (req,res) -> {
            String term = req.queryParams("term");
            if (term.length() == 0) {
                req.session().attribute("error","Необходимо ввести поисковый запрос");
                res.redirect("/notes");
                return true;
            }
            List<Note> list = data.stream()
                    .filter(s -> s.getText().contains(term) || s.getHead().contains(term))
                    .collect(Collectors.toList());
            Map<String, Object> model = new HashMap<>();
            model.put("data", list);
            return render(model);
        });
    }

    private static String render(Map<String, Object> model) {
        return new VelocityTemplateEngine().render(new ModelAndView(model, "notes.vm"));
    }
}