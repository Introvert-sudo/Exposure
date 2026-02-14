package com.exposure;

import com.exposure.models.Bot;
import com.exposure.models.Mission;
import com.exposure.models.User;
import com.exposure.repositories.BotRepository;
import com.exposure.repositories.MissionRepository;
import com.exposure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final BotRepository botRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("test").isEmpty()) {
            User testUser = new User("test", "test");
            userRepository.save(testUser);
        }

        if (botRepository.count() == 0) {
            Bot botHarry = new Bot("Harry", "Very polite man");
            Bot botMax = new Bot("Max", "Very rude man");

            botRepository.save(botHarry);
            botRepository.save(botMax);
        }

        if (missionRepository.count() == 0) {
            Mission default_mission = new Mission(
                    "The Midnight Masquerade Betrayal",
                    """
                    The Victim: Lord Julian Blackwood.
                    Cause of death: Rapid-acting toxin found in his wine glass.
                    Time of Death: Exactly 00:00, during the midnight toast.
                    Key Evidence:
                        The Bottle: Investigators noticed the vintage wine bottle on the serving table had a slightly crooked label, suggesting it might have been tampered with or swapped.
                        The Discrepancy: Both suspects were spotted near the drinks station at 23:55.
                        The Sound: A maid reported hearing a heavy object being thrown into the library fireplace moments before the toast.
                    
                    Suspects on Site:
                        The Heir: Currently drowning in debt to offshore casinos. He stands to inherit the entire estate tonight.
                        The Personal Doctor: Recently fired after a private shouting match with the Lord. He claims he was only trying to "save" him.
                    
                    Your Objective: Use your limited questions to cross-reference their movements between 23:50 and 00:00. Watch for emotional inconsistencies—one of them is faking their grief.
                    """,
                    """
                    history_description": "Lord Julian Blackwood was poisoned at 00:00 during the main toast. The poison (cyanide) was in a vintage 1945 wine bottle.
                    CRIME LOGIC: The killer swapped the original bottle with a poisoned one 5 minutes before the toast.
                    
                    The Victim: Lord Julian Blackwood.
                    Cause of death: Rapid-acting toxin found in his wine glass.
                    Time of Death: Exactly 00:00, during the midnight toast.
                    
                    Key Evidence:
                    1. The Bottle: A vintage wine bottle with a strangely crooked label.
                    2. The Discrepancy: Both suspects were seen at the drinks station at 23:55, but their stories don't match.
                    3. The Sound: A maid heard a heavy object being thrown into the library fireplace just before the toast.
                    
                    GENERATE TIMELINE & BEHAVIOR:
                    - Each character must provide a conflicting account of who was standing closest to the wine at 23:55.
                    - The killer (Role 1 or 2) must exhibit extreme emotional distress (sobbing, shaking) as a defensive mechanism.
                    - Characters should subtly mention a 'clink' sound or the missing silver tray.
                    
                    SUSPECTS:
                    1. Heir: Desperate for inheritance due to debt to the 'Iron Bank'. Seen near the drinks at 23:50.
                    2. Ex-doctor: Fired for 'knowing too much' about the Lord's terminal illness.
                    AI INSTRUCTIONS:
                    - Each bot must provide a slightly different timeline of who was at the drinks table between 23:45 and 00:00.
                    - The killer MUST act overly devastated (sobbing, hands shaking) to mask guilt.
                    - Bots should mention a 'clink' sound or a 'missing silver tray' to hint at the bottle swap.
                    - Do not admit the swap directly; only describe the 'unusual' appearance of the bottle label.
                    """,
                    2,
                    15
            );


            missionRepository.save(default_mission);
        }
    }
}
