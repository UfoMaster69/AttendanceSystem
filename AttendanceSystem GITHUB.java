    import java.io.*;
    import java.util.*;
    import java.time.LocalDate;

    class Student {
        String id;
        String name;
        int streak;
        int totalDays;
        int presentDays;

        Student(String id, String name) {
            this.id = id;
            this.name = name;
            this.streak = 0;
            this.totalDays = 0;
            this.presentDays = 0;
        }

        double getAttendancePercentage() {
            if (totalDays == 0) return 100.0;
            return (presentDays * 100.0) / totalDays;
        }
    }

    public class AttendanceSystem {
        static Scanner sc = new Scanner(System.in);
        static ArrayList<Student> students = new ArrayList<>();
        static HashMap<String, HashMap<String, String>> attendance = new HashMap<>();
        
        // Database for teacher accounts
        static HashMap<String, String> teacherAccounts = new HashMap<>();
        static HashMap<String, String> teacherSections = new HashMap<>(); 
        static final String TEACHER_FILE = "teachers.txt";
        static String currentTeacher = null; 
        
        // HARDCODED ADMIN CREDENTIALS
        static final String ADMIN_USER = "GROUP 2 ADMIN ACCOUNT";
        static final String ADMIN_PASS = "GROUP2ADMIN12345";

        public static void main(String[] args) {
            loadTeacherAccounts();
            
            boolean exitProgram = false;

            while (!exitProgram) {
                System.out.println("\n^=============ATTENDANCE MONITORING SYSTEM==============^");
                System.out.println("1. TEACHER ACCOUNT LOGIN");
                System.out.println("2. ADMIN ACCOUNT LOGIN");
                System.out.println("3. EXIT");
                System.out.print("CHOICES # : ");

                int choice = 0;
                if (sc.hasNextInt()) {
                    choice = sc.nextInt();
                    sc.nextLine();
                } else {
                    sc.nextLine();
                    System.out.println("INVALID input! Enter 1-3.");
                    continue;
                }

                switch (choice) {
                    case 1:
                        if (teacherAccounts.isEmpty()) {
                            System.out.println("No teacher accounts found. Please ask Admin to create one.");
                        } else {
                            if (teacherLogin()) {
                                loadStudents();
                                loadAttendance();
                                teacherMenu();
                                clearSessionData();
                            }
                        }
                        break;
                    case 2:
                        if (adminLogin()) {
                            adminMenu();
                        }
                        break;
                    case 3:
                        exitProgram = true;
                        System.out.println("Exiting program...");
                        break;
                    default:
                        System.out.println("Invalid Choice.");
                }
            }
        }

        // ================= ACCOUNT MANAGEMENTS =================
        
        static void loadTeacherAccounts() {
            File file = new File(TEACHER_FILE);
            if (!file.exists()) return;
            try (Scanner fileScanner = new Scanner(file)) {
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        teacherAccounts.put(parts[0], parts[1]);
                        teacherSections.put(parts[0], parts.length == 3 ? parts[2] : "Unassigned");
                    }
                }
            } catch (Exception e) {
                System.out.println("Error loading teacher credentials.");
            }
        }

        static void saveAllTeachers() {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(TEACHER_FILE))) {
                for (String username : teacherAccounts.keySet()) {
                    String pass = teacherAccounts.get(username);
                    String sec = teacherSections.getOrDefault(username, "Unassigned");
                    bw.write(username + "," + pass + "," + sec);
                    bw.newLine();
                }
            } catch (Exception e) {
                System.out.println("Error saving teacher credentials.");
            }
        }

        static boolean adminLogin() {
            System.out.println("\n===== ADMIN LOGIN =====");
            System.out.print("Username (Type 'E' to Cancel): ");
            String username = sc.nextLine().trim();
            if (username.equalsIgnoreCase("E")) { System.out.println("Cancelled."); return false; }
            
            System.out.print("Password: ");
            String password = sc.nextLine().trim();

            if (username.equals(ADMIN_USER) && password.equals(ADMIN_PASS)) {
                System.out.println("Login Successful. Welcome, Admin!");
                return true;
            } else {
                System.out.println("Invalid Admin credentials.");
                return false;
            }
        }

        static boolean teacherLogin() {
            System.out.println("\n===== TEACHER LOGIN =====");
            System.out.print("Username (Type 'E' to Cancel): ");
            String username = sc.nextLine().trim();
            if (username.equalsIgnoreCase("E")) { System.out.println("Cancelled."); return false; }

            System.out.print("Password: ");
            String password = sc.nextLine().trim();

            if (teacherAccounts.containsKey(username) && teacherAccounts.get(username).equals(password)) {
                System.out.println("Login Successful. Welcome, " + username + "!");
                currentTeacher = username;
                return true;
            } else {
                System.out.println("Invalid Teacher credentials.");
                return false;
            }
        }

        // ================= MENUS =================
        
        static void adminMenu() {
            boolean logout = false;
            while (!logout) {
                System.out.println("\n===== ADMIN MENU =====");
                System.out.println("1. Create Teacher Account");
                System.out.println("2. Register Student to a Class");
                System.out.println("3. Remove/Delete Student from a Class");
                System.out.println("4. View Teacher's Class / Search Students");
                System.out.println("5. Manage/Assign Section to Teacher");
                System.out.println("6. View / Edit Teacher Account Details (Name, Password)");
                System.out.println("7. Logout");
                System.out.print("CHOICES # : ");

                int choice = 0;
                if (sc.hasNextInt()) {
                    choice = sc.nextInt();
                    sc.nextLine();
                } else {
                    sc.nextLine();
                    System.out.println("Invalid input. Please enter 1-7.");
                    continue;
                }

                switch (choice) {
                    case 1: createTeacherAccount(); break;
                    case 2: manageClassRecords("register"); break;
                    case 3: manageClassRecords("delete"); break; 
                    case 4: searchTeacherStudents(); break;
                    case 5: manageSection(); break;
                    case 6: manageTeacherAccountDetails(); break; // NEW FEATURE
                    case 7:
                        logout = true;
                        System.out.println("Admin logged out.");
                        break;
                    default: 
                        System.out.println("Invalid selection.");
                }
            }
        }

        static void teacherMenu() {
            boolean logout = false;
            while (!logout) {
                String sectionName = teacherSections.getOrDefault(currentTeacher, "Unassigned");
                System.out.println("\n===== TEACHER MENU (" + currentTeacher + " | Sec: " + sectionName + ") =====");
                System.out.println("1. Check/Record Attendance");
                System.out.println("2. View Students");
                System.out.println("3. View All Attendance Records");
                System.out.println("4. View Attendance by Specific Date");
                System.out.println("5. Streak Leaderboard");
                System.out.println("6. Logout");
                System.out.print("CHOICES # : ");

                int choice = 0;
                if (sc.hasNextInt()) {
                    choice = sc.nextInt();
                    sc.nextLine();
                } else {
                    sc.nextLine();
                    System.out.println("Invalid input. Please enter 1-6.");
                    continue;
                }

                switch (choice) {
                    case 1: recordAttendance(); saveAttendance(); saveStudents(); break;
                    case 2: viewStudents(); break;
                    case 3: viewAttendance(); break;
                    case 4: viewAttendanceByDate(); break;
                    case 5: showTopStreak(); break;
                    case 6:
                        logout = true;
                        System.out.println("Teacher logged out.");
                        break;
                    default: System.out.println("Invalid selection.");
                }
            }
        }

        // ================= ADMIN FUNCTIONS =================

        static void createTeacherAccount() {
            System.out.println("\n===== CREATE TEACHER ACCOUNT =====");
            String username;
            while (true) {
                System.out.print("Enter Teacher Username (Type 'E' to Cancel): ");
                username = sc.nextLine().trim();
                
                if (username.equalsIgnoreCase("E")) {
                    System.out.println("Cancelled account creation.");
                    return;
                }
                
                if (username.isEmpty()) System.out.println("Username cannot be empty!");
                // UPDATED REGEX: Added a period (.) so it accepts formats like "Watson Humphrey T. Repaso"
                else if (!username.matches("[a-zA-Z0-9_ .]+")) System.out.println("Invalid characters in username! Pwede lang ang letters, numbers, spaces, at period (.).");
                else if (teacherAccounts.containsKey(username)) System.out.println("Username already exists!");
                else break;
            }

            String password;
            while (true) {
                System.out.print("Enter Password (Type 'E' to Cancel): ");
                password = sc.nextLine().trim();
                if (password.equalsIgnoreCase("E")) { System.out.println("Cancelled."); return; }
                if (password.isEmpty()) System.out.println("Password cannot be empty!");
                else break;
            }

            String section;
            while (true) {
                System.out.print("Enter Section Name (Letters, Numbers, '-' only) or 'E' to Cancel: ");
                section = sc.nextLine().trim();
                if (section.equalsIgnoreCase("E")) { System.out.println("Cancelled."); return; }
                if (section.isEmpty()) System.out.println("Section cannot be empty!");
                else if (!section.matches("[a-zA-Z0-9-]+")) System.out.println("Invalid format! Letters, Numbers, at '-' lang ang pwede.");
                else break;
            }

            teacherAccounts.put(username, password);
            teacherSections.put(username, section);
            saveAllTeachers();
            System.out.println("Teacher account successfully created with Section: " + section);

            System.out.print("Do you want to register students for " + username + " now? (Y/N): ");
            if (sc.nextLine().trim().equalsIgnoreCase("Y")) {
                currentTeacher = username;
                loadStudents(); 
                registerStudent();
                saveStudents();
                clearSessionData();
            }
        }

        static void manageTeacherAccountDetails() {
            if (teacherAccounts.isEmpty()) {
                System.out.println("No teacher accounts exist yet.");
                return;
            }

            System.out.println("\n===== VIEW / EDIT TEACHER ACCOUNT =====");
            System.out.print("Enter Teacher Username to manage (Type 'E' to Cancel): ");
            String tUser = sc.nextLine().trim();

            if (tUser.equalsIgnoreCase("E")) return;

            if (!teacherAccounts.containsKey(tUser)) {
                System.out.println("Teacher account not found.");
                return;
            }

            boolean back = false;
            while (!back) {
                System.out.println("\n--- ACCOUNT DETAILS ---");
                System.out.println("Username : " + tUser);
                System.out.println("Password : " + teacherAccounts.get(tUser)); // Recover password feature
                System.out.println("Section  : " + teacherSections.getOrDefault(tUser, "Unassigned"));
                System.out.println("-----------------------");
                System.out.println("1. Change Username");
                System.out.println("2. Change Password");
                System.out.println("E. Back to Admin Menu");
                System.out.print("CHOICES # : ");
                
                String choiceStr = sc.nextLine().trim().toUpperCase();
                if (choiceStr.equals("E")) {
                    back = true;
                    continue;
                }

                if (choiceStr.equals("1")) {
                    System.out.print("Enter New Username (Type 'E' to Cancel): ");
                    String newName = sc.nextLine().trim();
                    
                    if (newName.equalsIgnoreCase("E")) continue;
                    // UPDATED REGEX: Added period (.) here as well
                    if (newName.isEmpty() || !newName.matches("[a-zA-Z0-9_ .]+")) {
                        System.out.println("Invalid format. Use letters, numbers, periods, and spaces only.");
                    } else if (teacherAccounts.containsKey(newName)) {
                        System.out.println("Username already exists! Choose a different one.");
                    } else {
                        // Transfer data to new username map keys
                        String oldPass = teacherAccounts.remove(tUser);
                        String oldSec = teacherSections.remove(tUser);
                        teacherAccounts.put(newName, oldPass);
                        teacherSections.put(newName, oldSec);
                        
                        // Rename database files to avoid losing student records!
                        File oldStudentFile = new File(tUser + "_students_db.txt");
                        File newStudentFile = new File(newName + "_students_db.txt");
                        if (oldStudentFile.exists()) oldStudentFile.renameTo(newStudentFile);

                        File oldAttFile = new File(tUser + "_attendance_db.txt");
                        File newAttFile = new File(newName + "_attendance_db.txt");
                        if (oldAttFile.exists()) oldAttFile.renameTo(newAttFile);

                        saveAllTeachers();
                        System.out.println("Username successfully changed from '" + tUser + "' to '" + newName + "'");
                        tUser = newName; // Update active variable
                    }
                } else if (choiceStr.equals("2")) {
                    System.out.print("Enter New Password (Type 'E' to Cancel): ");
                    String newPass = sc.nextLine().trim();
                    if (newPass.equalsIgnoreCase("E")) continue;
                    if (newPass.isEmpty()) {
                        System.out.println("Password cannot be empty.");
                    } else {
                        teacherAccounts.put(tUser, newPass);
                        saveAllTeachers();
                        System.out.println("Password successfully updated!");
                    }
                } else {
                    System.out.println("Invalid choice.");
                }
            }
        }

        static void manageClassRecords(String action) {
            if (teacherAccounts.isEmpty()) {
                System.out.println("No teacher accounts exist yet.");
                return;
            }

            System.out.println("\n===== SELECT TEACHER ACCOUNT =====");
            System.out.print("Enter Teacher Username to manage records (Type 'E' to Cancel): ");
            String tUser = sc.nextLine().trim();

            if (tUser.equalsIgnoreCase("E")) {
                System.out.println("Action cancelled.");
                return;
            }

            if (teacherAccounts.containsKey(tUser)) {
                currentTeacher = tUser;
                loadStudents();
                loadAttendance();
                
                if (action.equals("register")) {
                    registerStudent();
                    saveStudents();
                } else if (action.equals("delete")) {
                    deleteStudent();
                }

                clearSessionData();
            } else {
                System.out.println("Teacher account not found.");
            }
        }

        static void searchTeacherStudents() {
            if (teacherAccounts.isEmpty()) {
                System.out.println("No teacher accounts exist yet.");
                return;
            }

            System.out.println("\n===== SEARCH TEACHER'S CLASS =====");
            System.out.println("List of Registered Teachers:");
            for (String t : teacherAccounts.keySet()) {
                System.out.println(" -> " + t + " (Section: " + teacherSections.getOrDefault(t, "Unassigned") + ")");
            }

            System.out.print("\nEnter Teacher Username to view their students (Type 'E' to Cancel): ");
            String tUser = sc.nextLine().trim();

            if (tUser.equalsIgnoreCase("E")) {
                System.out.println("Action cancelled.");
                return;
            }

            if (teacherAccounts.containsKey(tUser)) {
                currentTeacher = tUser;
                loadStudents();
                
                System.out.println("\n===== CLASS LIST FOR: " + tUser + " =====");
                if (students.isEmpty()) {
                    System.out.println("No students registered in this class yet.");
                } else {
                    for (Student s : students) {
                        System.out.printf("ID: %s | Name: %s\n", s.id, s.name);
                    }
                }
                clearSessionData();
            } else {
                System.out.println("Teacher account not found.");
            }
        }

        static void manageSection() {
            System.out.println("\n===== MANAGE TEACHER'S SECTION =====");
            System.out.print("Enter Teacher Username (Type 'E' to Cancel): ");
            String tUser = sc.nextLine().trim();
            
            if (tUser.equalsIgnoreCase("E")) return;
            
            if (!teacherAccounts.containsKey(tUser)) {
                System.out.println("Teacher not found!");
                return;
            }
            
            System.out.println("Current Section: " + teacherSections.getOrDefault(tUser, "Unassigned"));
            
            String newSection;
            while (true) {
                System.out.print("Enter new Section Name (Letters, Numbers, '-' only) or 'E' to Cancel: ");
                newSection = sc.nextLine().trim();
                
                if (newSection.equalsIgnoreCase("E")) {
                    System.out.println("Action cancelled.");
                    return;
                }
                if (newSection.isEmpty()) System.out.println("Cannot be empty.");
                else if (!newSection.matches("[a-zA-Z0-9-]+")) System.out.println("Invalid format! Only letters, numbers, and hyphens allowed.");
                else break;
            }
            
            teacherSections.put(tUser, newSection);
            saveAllTeachers();
            System.out.println("Section updated successfully for " + tUser + "!");
        }

        static void registerStudent() {
            boolean addMore = true;
            while (addMore) {
                System.out.println("\n===== REGISTER STUDENT FOR: " + currentTeacher + " =====");
                
                String id = generateRandomID();
                System.out.println("Assigned Auto-Generated ID: " + id);

                String name = "";
                while (true) {
                    System.out.print("Full Name (Type 'E' to Cancel): ");
                    name = sc.nextLine().trim();
                    if (name.equalsIgnoreCase("E")) {
                        System.out.println("Registration cancelled.");
                        return;
                    }
                    if (name.isEmpty()) System.out.println("Field cannot be empty.");
                    // Update regex here too in case you want to allow initials for students!
                    else if (!name.matches("[a-zA-Z .]+")) System.out.println("Letters, periods, and spaces only.");
                    else if (isDuplicateName(name)) System.out.println("Name already registered.");
                    else break;
                }

                students.add(new Student(id, name));
                System.out.println("Registration successful.");

                System.out.print("Register another student for this class? (Y/N): ");
                String response = sc.nextLine().trim().toUpperCase();
                if (!response.equals("Y")) addMore = false;
            }
        }

        static void deleteStudent() {
            if (students.isEmpty()) {
                System.out.println("No records found in " + currentTeacher + "'s class.");
                return;
            }

            System.out.println("\n===== DELETE RECORD FOR: " + currentTeacher + " =====");
            System.out.print("Enter Student ID or NAME to remove (Type 'E' to Cancel): ");
            String inputToRemove = sc.nextLine().trim();
            
            if (inputToRemove.equalsIgnoreCase("E")) {
                System.out.println("Action cancelled.");
                return;
            }

            Student target = null;
            for (Student s : students) {
                if (s.id.equals(inputToRemove) || s.name.equalsIgnoreCase(inputToRemove)) {
                    target = s;
                    break;
                }
            }

            if (target != null) {
                System.out.print("Confirm deletion for " + target.name + "? (Y/N): ");
                if (sc.nextLine().trim().toUpperCase().equals("Y")) {
                    students.remove(target);
                    
                    for (String date : attendance.keySet()) {
                        attendance.get(date).remove(target.id);
                    }
                    saveStudents(); 
                    saveAttendance();
                    System.out.println("Student record deleted.");
                } else {
                    System.out.println("Action aborted.");
                }
            } else {
                System.out.println("ID or Name not found in the database.");
            }
        }

        // ================= TEACHER / CORE FEATURES =================
        
        static void recordAttendance() {
            if (students.isEmpty()) { System.out.println("No students found. Admin needs to register students first."); return; }
            
            System.out.println("\n===== RECORD / UPDATE ATTENDANCE =====");
            System.out.print("Enter Date (YYYY-MM-DD), press ENTER for today (" + LocalDate.now() + "), or 'E' to Cancel: ");
            String date = sc.nextLine().trim();
            
            if (date.equalsIgnoreCase("E")) {
                System.out.println("Attendance recording cancelled.");
                return;
            }
            
            if (date.isEmpty()) {
                date = LocalDate.now().toString();
            }

            attendance.putIfAbsent(date, new HashMap<>());
            HashMap<String, String> daily = attendance.get(date);

            System.out.println("Recording for date: " + date);
            System.out.println("NOTE: Students already marked 'Present' or 'Late' will not appear.");

            int pendingCount = 0;

            for (Student s : students) {
                String currentStatus = daily.getOrDefault(s.id, "No Record");
                
                if (currentStatus.equals("Present") || currentStatus.equals("Late")) {
                    continue; 
                }

                pendingCount++;
                System.out.println("\nStudent: " + s.name + " [" + s.id + "]");
                System.out.println("Current Status: " + currentStatus);
                System.out.println("1. Present  2. Absent  3. Late  E. Save & Stop Marking");
                
                int choice = -1;
                while (true) {
                    System.out.print("CHOICES # : ");
                    String choiceStr = sc.nextLine().trim().toUpperCase();
                    
                    if (choiceStr.equals("E")) {
                        System.out.println("Saving records and stopping attendance for now...");
                        choice = 0; 
                        break;
                    }
                    
                    try {
                        choice = Integer.parseInt(choiceStr);
                        if (choice >= 1 && choice <= 3) break;
                        else System.out.println("Invalid input. Select 1-3 or E.");
                    } catch (Exception e) {
                        System.out.println("Invalid input. Select 1-3 or E.");
                    }
                }

                if (choice == 0) break; 

                if (currentStatus.equals("No Record")) {
                    s.totalDays++;
                    if (choice == 1 || choice == 3) s.presentDays++;
                } else if (currentStatus.equals("Absent") && (choice == 1 || choice == 3)) {
                    s.presentDays++;
                } else if ((currentStatus.equals("Present") || currentStatus.equals("Late")) && choice == 2) {
                    s.presentDays--; 
                }

                if (choice == 1) { 
                    daily.put(s.id, "Present"); 
                    s.streak++; 
                } else if (choice == 2) { 
                    daily.put(s.id, "Absent"); 
                    s.streak = 0; 
                } else if (choice == 3) {
                    daily.put(s.id, "Late");
                    s.streak++; 
                }
            }
            
            if (pendingCount == 0) {
                System.out.println("\nAll students have already been marked Present/Late for this date!");
            } else {
                System.out.println("\nAttendance logged successfully for " + date + "!");
            }
        }

        static void viewStudents() {
            if (students.isEmpty()) { System.out.println("No records found."); return; }
            System.out.println("\n===== STUDENT DATABASE =====");
            for (Student s : students)
                System.out.printf("ID: %s | Name: %-15s | Streak: %d | Percentage: %.2f%%\n",
                        s.id, s.name, s.streak, s.getAttendancePercentage());
        }

        static void viewAttendance() {
            if (attendance.isEmpty()) { System.out.println("No logs available."); return; }
            System.out.println("\n===== LOG HISTORY =====");
            for (String date : attendance.keySet()) {
                System.out.println("\nDate: " + date);
                HashMap<String,String> daily = attendance.get(date);
                for (Student s : students) {
                    String status = daily.getOrDefault(s.id, "N/A");
                    System.out.println(s.name + " -> " + status);
                }
            }
        }

        static void viewAttendanceByDate() {
            if (attendance.isEmpty()) { 
                System.out.println("No attendance logs available yet."); 
                return; 
            }
            
            System.out.println("\n===== AVAILABLE DATES =====");
            ArrayList<String> dates = new ArrayList<>(attendance.keySet());
            Collections.sort(dates); 
            
            for (int i = 0; i < dates.size(); i++) {
                System.out.println((i + 1) + ". " + dates.get(i));
            }
            System.out.println("E. Cancel / Back");
            
            System.out.print("\nSelect Date # : ");
            String choiceStr = sc.nextLine().trim().toUpperCase();
            
            if (choiceStr.equals("E")) {
                System.out.println("Cancelled.");
                return;
            }

            int choice = -1;
            try {
                choice = Integer.parseInt(choiceStr);
            } catch (Exception e) {
                System.out.println("Invalid input.");
                return;
            }

            if (choice > 0 && choice <= dates.size()) {
                String selectedDate = dates.get(choice - 1);
                System.out.println("\n===== ATTENDANCE FOR " + selectedDate + " =====");
                HashMap<String, String> daily = attendance.get(selectedDate);
                for (Student s : students) {
                    String status = daily.getOrDefault(s.id, "No Record");
                    System.out.printf("%-15s [%s] -> %s\n", s.name, s.id, status);
                }
            } else {
                System.out.println("Invalid selection. Returning to menu.");
            }
        }

        static void showTopStreak() {
            if (students.isEmpty()) { System.out.println("No active students."); return; }
            
            ArrayList<Student> sortedList = new ArrayList<>(students);
            sortedList.sort((a,b) -> b.streak - a.streak);
            
            System.out.println("\n===== LEADERBOARD =====");
            int rank = 1;
            for (Student s : sortedList) {
                if(s.streak > 0) {
                    System.out.printf("%d. %-15s | Streak: %d days | Percentage: %.2f%%\n",
                            rank++, s.name, s.streak, s.getAttendancePercentage());
                }
            }
            if(rank == 1) System.out.println("No active streaks found.");
        }

        // ================= UTILITIES =================

        static String generateRandomID() {
            Random rand = new Random();
            String id;
            do { 
                int part1 = rand.nextInt(100);       
                int part2 = rand.nextInt(10000);     
                int part3 = rand.nextInt(100000);    
                
                id = String.format("%02d-%04d-%05d", part1, part2, part3);
            } while (isDuplicateID(id));
            return id;
        }

        static boolean isDuplicateID(String id) {
            for (Student s : students) if (s.id.equals(id)) return true;
            return false;
        }

        static boolean isDuplicateName(String name) {
            for (Student s : students) if (s.name.equalsIgnoreCase(name)) return true;
            return false;
        }

        // ================= DATA PERSISTENCE =================
        
        static void clearSessionData() {
            students.clear();
            attendance.clear();
            currentTeacher = null;
        }
        
        static String getStudentFile() { 
            return currentTeacher + "_students_db.txt"; 
        }
        
        static String getAttendanceFile() { 
            return currentTeacher + "_attendance_db.txt"; 
        }

        static void loadStudents() {
            students.clear();
            if (currentTeacher == null) return;
            
            File file = new File(getStudentFile());
            if (!file.exists()) return;
            try (Scanner scFile = new Scanner(file)) {
                while (scFile.hasNextLine()) {
                    String[] parts = scFile.nextLine().split(",");
                    if (parts.length == 5) {
                        Student s = new Student(parts[0], parts[1]);
                        s.streak = Integer.parseInt(parts[2]);
                        s.totalDays = Integer.parseInt(parts[3]);
                        s.presentDays = Integer.parseInt(parts[4]);
                        students.add(s);
                    }
                }
            } catch (Exception e) { System.out.println("Data loading error."); }
        }

        static void saveStudents() {
            if (currentTeacher == null) return;
            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(getStudentFile()))) {
                for (Student s : students)
                    bw.write(s.id + "," + s.name + "," + s.streak + "," + s.totalDays + "," + s.presentDays + "\n");
            } catch (Exception e) { System.out.println("Data saving error."); }
        }

        static void loadAttendance() {
            attendance.clear();
            if (currentTeacher == null) return;
            
            File file = new File(getAttendanceFile());
            if (!file.exists()) return;
            try (Scanner scFile = new Scanner(file)) {
                while (scFile.hasNextLine()) {
                    String[] parts = scFile.nextLine().split(",");
                    if (parts.length == 3) {
                        attendance.putIfAbsent(parts[0], new HashMap<>());
                        attendance.get(parts[0]).put(parts[1], parts[2]);
                    }
                }
            } catch (Exception e) { System.out.println("Attendance data error."); }
        }

        static void saveAttendance() {
            if (currentTeacher == null) return;
            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(getAttendanceFile()))) {
                for (String date : attendance.keySet()) {
                    HashMap<String,String> daily = attendance.get(date);
                    for (String id : daily.keySet())
                        bw.write(date + "," + id + "," + daily.get(id) + "\n");
                }
            } catch (Exception e) { System.out.println("Record sync error."); }
        }
    }