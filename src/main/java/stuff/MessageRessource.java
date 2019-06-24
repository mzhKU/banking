package stuff;

// Relation "MessageRessource"

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Path("messages")
public class MessageRessource {
    private static Map<Integer, Message> map;
    private static AtomicInteger counter = new AtomicInteger();

    static {
        map = new ConcurrentHashMap<>();
        Instant instant = Instant.now();
        map.put(1, new Message(1, "Hoi", instant));
        map.put(2, new Message(2, "Sali", instant.plusSeconds(1)));
        map.put(3, new Message(3, "Was lauft?", instant.plusSeconds(2)));
        counter.set(3);
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getAllMessagesAsText() {
        StringBuilder stringBuilder = new StringBuilder();
        map.values().stream()
                .sorted((m1, m2) -> -m1.getTimestamp().compareTo(m2.getTimestamp()))
                .forEach(m -> stringBuilder.append(m.toString() + "\n"));
        return stringBuilder.toString();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getMessageAsText(@PathParam("id") int id) {
        Message message = map.get(id);
        if(message == null) {
            return Response.status(404).build();
            //     Response.noContent().status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(message.toString()).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getAllMessagesAsHtml() {

        String htmlHead      = "<html><head><meta charset=\"UTF-8\"></head>";
        String htmlBodyStart = "<body><table border='1'>";
        String htmlBodyEnd   = "</table></body></html>";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(htmlHead);
        stringBuilder.append(htmlBodyStart);

        map.values().stream()
                .sorted((m1, m2) -> -m1.getTimestamp().compareTo(m2.getTimestamp()))
                .forEach(m -> stringBuilder.append("<tr><td>")
                        .append(m.getId())
                        .append("</td><td>")
                        .append(m.getText())
                        .append("</td><td>")
                        .append(m.getTimestamp())
                        .append("</td></tr>")
                );

        stringBuilder.append(htmlBodyEnd);

        return stringBuilder.toString();
    }

}
