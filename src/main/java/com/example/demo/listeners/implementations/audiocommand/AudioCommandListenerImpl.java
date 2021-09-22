package com.example.demo.listeners.implementations.audiocommand;


import com.example.demo.listeners.AudioCommandListener;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AudioCommandListenerImpl implements AudioCommandListener {

    @Autowired
    private AudioCommandService audioService;

    // used to check if audio connection has already been called
    // this is so that the bot doesn't reconnect to play/add a song to the queue
    private boolean checker = true;

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        if(messageCreateEvent.getMessageAuthor().isBotUser()) return;

        if(!messageCreateEvent.getMessageAuthor().getConnectedVoiceChannel().isPresent()) {
            return;
        }

        // the input of the user
        String text = messageCreateEvent.getMessageContent();

        if(text.startsWith("!play")) {

            if(checker) {
                checker = false;
                audioService.startAudioConnection(messageCreateEvent);

                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            audioService.playTrack(messageCreateEvent, text);
        }

        if(!isBotConnected(messageCreateEvent)) return;

        // disconnects the bot and clears the queue
        if(text.equalsIgnoreCase("!dc")) {
            audioService.disconnectBot();
            checker = true;
        }

        // show the songs in the queue
        if(text.equalsIgnoreCase("!q")) {
            audioService.getQueue(messageCreateEvent);
        }

        // clear the queue
        if(text.equalsIgnoreCase("!qclear")) {
            audioService.clearQueue(messageCreateEvent);
        }

        // skip a song
        if(text.equalsIgnoreCase("!s")) {
            audioService.skipTrack();
        }

        // seeks the song
        if(text.startsWith("!seek")) {
            if(text.length() > 6) {
                String timeInSeconds = text.substring(6);
                if(isInteger(timeInSeconds)) {
                    int seconds = Integer.parseInt(timeInSeconds);
                    if(seconds < 0) return;

                    audioService.seek(seconds, messageCreateEvent);
                }
            }
        }
    }

    private boolean isBotConnected(MessageCreateEvent messageCreateEvent) {
        return messageCreateEvent.getMessageAuthor().getConnectedVoiceChannel().get().isConnected(833896293863915570L);
    }

    public boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        }
        catch(Exception e) {
            return false;
        }
    }
}
