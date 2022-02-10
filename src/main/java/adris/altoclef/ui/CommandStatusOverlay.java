package adris.altoclef.ui;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class CommandStatusOverlay {

    //For the ingame timer
    private long _timeRunning;
    private long _lastTime = 0;

    public void render(AltoClef mod, MatrixStack matrixstack) {
        if (mod.getModSettings().shouldShowTaskChain()) {
            List<Task> tasks = Collections.emptyList();
            if (mod.getTaskRunner().getCurrentTaskChain() != null) {
                tasks = mod.getTaskRunner().getCurrentTaskChain().getTasks();
            }

            int color = 0xFFFFFFFF;
            drawTaskChain(MinecraftClient.getInstance().textRenderer, matrixstack, 0, 0, color, 10, tasks, mod);
        }
    }
    private DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.from(ZoneOffset.of("+00:00"))); // The date formatter
    private void drawTaskChain(TextRenderer renderer, MatrixStack stack, float dx, float dy, int color, int maxLines, List<Task> tasks, AltoClef mod) {
        if (tasks.size() == 0) {
            renderer.draw(stack, " (no task running) ", dx, dy, color);
            if (_lastTime+10000 < Instant.now().toEpochMilli() && mod.getModSettings().shouldShowTimer()) {//if it doesn't run any task in 10 secs
                _timeRunning = Instant.now().toEpochMilli();//reset the timer
            }
        } else {
            float fontHeight = renderer.fontHeight;
            if (mod.getModSettings().shouldShowTimer()) { //If it's enabled
                _lastTime = Instant.now().toEpochMilli(); //keep the last time for the timer reset
                String _realTime = DATE_TIME_FORMATTER.format(Instant.now().minusMillis(_timeRunning)); //Format the running time to string
                renderer.draw(stack, "<"+_realTime+">", dx, dy, color);//Draw the timer before drawing tasks list
                dx += 8;//Do the same thing to list the tasks
                dy += fontHeight + 2;
            }
            if (tasks.size() > maxLines) {
                // Preserve the first task and as many tasks from the end as needed to fit the max lines
                // Eliminate as many tasks without targets as needed to fit the max lines
                List<Task> tasksToDisplay = new ArrayList<>(tasks);
                for (int i = 0; i < tasks.size(); i++) {
                    boolean hasTarget = ResourceTask.class.isAssignableFrom(tasksToDisplay.get(i).getClass());
                    if (!hasTarget) {
                        tasksToDisplay.remove(i);
                        i--;
                        break;
                    }
                    if (tasksToDisplay.size() == maxLines) {
                        break;
                    }
                }
                // If that doesn't work, just eliminate as many as needed to fit the max lines
                // Now render the tasks
                // eg [1, 2, 3, 4, 5] with maxLines = 3 would be rendered as [1, ..., 5]
                for (int i = 0; i < tasksToDisplay.size(); i++) {
                    Task task = tasksToDisplay.get(i);
                    if (i == 0 || i > tasksToDisplay.size() - maxLines) {
                        renderer.draw(stack, tasks.get(i).toString(), dx, dy, color);
                    } else if (i == 1) {
                        renderer.draw(stack, "...", dx, dy, color);
                    } else {
                        continue;
                    }
                    dx += 8;
                    dy += fontHeight + 2;
                }
            } else {
                for (Task task : tasks) {
                    renderer.draw(stack, task.toString(), dx, dy, color);
                    dx += 8;
                    dy += fontHeight + 2;
                }
            }

        }
    }
}
