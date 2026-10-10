package com.retrievo.config;

import com.retrievo.entity.*;
import com.retrievo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemMatchRepository itemMatchRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // Create Admin User
            User admin = new User("System Administrator", "admin@retrievo.app", passwordEncoder.encode("Admin@123"), "+1555000111", Role.ADMIN);
            admin = userRepository.save(admin);

            // Create Demo Student User
            User student = new User("Alex Johnson", "student@university.edu", passwordEncoder.encode("User@123"), "+1555222333", Role.USER);
            student = userRepository.save(student);

            // Create Demo Finder User
            User finder = new User("Sarah Smith", "sarah@university.edu", passwordEncoder.encode("User@123"), "+1555444555", Role.USER);
            finder = userRepository.save(finder);

            // Create Lost Item 1
            Item lostLaptop = new Item();
            lostLaptop.setReporter(student);
            lostLaptop.setItemType(ItemType.LOST);
            lostLaptop.setTitle("Dark Gray MacBook Pro 14-inch");
            lostLaptop.setCategory("Electronics");
            lostLaptop.setDescription("Space Gray MacBook Pro with M2 Chip. Has a metallic developer sticker on the lid.");
            lostLaptop.setLocation("Central Library 3rd Floor Quiet Zone");
            lostLaptop.setDateLostOrFound(LocalDate.now().minusDays(2));
            lostLaptop.setApproximateTime("Between 3:00 PM and 5:00 PM");
            lostLaptop.setDistinguishingFeatures("Small scratch near USB-C port, unique developer sticker.");
            lostLaptop.setStatus(ItemStatus.OPEN);
            lostLaptop = itemRepository.save(lostLaptop);

            // Create Found Item 1
            Item foundLaptop = new Item();
            foundLaptop.setReporter(finder);
            foundLaptop.setItemType(ItemType.FOUND);
            foundLaptop.setTitle("Apple Laptop in Gray Sleeve");
            foundLaptop.setCategory("Electronics");
            foundLaptop.setDescription("Found a gray metallic Apple laptop left under a study desk. Turned into library desk.");
            foundLaptop.setLocation("Central Library Desk");
            foundLaptop.setDateLostOrFound(LocalDate.now().minusDays(1));
            foundLaptop.setApproximateTime("Around 6:15 PM");
            foundLaptop.setSafeStorageNotes("Held at Central Library Information Desk B");
            foundLaptop.setStatus(ItemStatus.OPEN);
            foundLaptop = itemRepository.save(foundLaptop);

            // Create Lost Item 2
            Item lostWallet = new Item();
            lostWallet.setReporter(student);
            lostWallet.setItemType(ItemType.LOST);
            lostWallet.setTitle("Brown Leather Bifold Wallet");
            lostWallet.setCategory("Wallets and Money");
            lostWallet.setDescription("Brown leather wallet containing student ID card and bus pass.");
            lostWallet.setLocation("Campus Cafeteria Main Hall");
            lostWallet.setDateLostOrFound(LocalDate.now().minusDays(3));
            lostWallet.setStatus(ItemStatus.OPEN);
            itemRepository.save(lostWallet);

            // Create Match Record
            ItemMatch match = new ItemMatch(lostLaptop, foundLaptop, 0.88, 0.82, 0.92, "WEIGHTED_FEATURE_FALLBACK");
            itemMatchRepository.save(match);
            lostLaptop.setStatus(ItemStatus.MATCHED);
            itemRepository.save(lostLaptop);

            // Create Notification
            Notification notification = new Notification(
                    student,
                    "AI Candidate Match Detected!",
                    "We found an 88% candidate match for your reported 'Dark Gray MacBook Pro'.",
                    "MATCH",
                    "/items/" + lostLaptop.getId()
            );
            notificationRepository.save(notification);
        }
    }
}
