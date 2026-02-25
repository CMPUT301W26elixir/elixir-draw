
# CRC Cards

## Actor Classes

### **Class: Entrant**
| Responsibility | Collaborators |
| :--- | :--- |
| - Maintain personal profile (Name, Contact, etc.) | Profile |
| - Scan QR codes to view event details | Event, QRScanner |
| - Join/Leave a Waiting List for an event | WaitingList, Event |
| - Provide geolocation data when joining (if required) | GPSManager, WaitingList |
| - Accept or decline an invitation after being chosen | Event, Notification |
| - View status (Waiting, Selected, Cancelled) | Event, Notification |

---

### **Class: Organizer**
| Responsibility | Collaborators |
| :--- | :--- |
| - Create and publish new events | Event, ImageUploader |
| - Generate QR codes for event promotion | QRCodeGenerator, Event |
| - Set lottery constraints (Capacity, Deadlines) | Event |
| - Trigger the "Sampling/Drawing" process | WaitingList, LotteryEngine |
| - Manage the final attendee list and cancel participants | Event, WaitingList |

---

### **Class: Administrator**
| Responsibility | Collaborators |
| :--- | :--- |
| - Manage system-wide infrastructure | FirebaseManager |
| - Remove inappropriate events or profiles | Event, Entrant |
| - Oversee system integrity and roles | Entrant, Organizer |

---

## Core Logic & Data Classes

### **Class: Event**
| Responsibility | Collaborators |
| :--- | :--- |
| - Store metadata (Name, Price, Dates, Poster) | ImageUploader |
| - Link to a specific Organizer | Organizer |
| - Manage its own Waiting List and Attendee List | WaitingList, Entrant |
| - Track registration status (Open/Closed) | LotteryEngine |

---

### **Class: WaitingList**
| Responsibility | Collaborators |
| :--- | :--- |
| - Hold a collection of interested Entrants | Entrant |
| - Store geolocation data for each entrant entry | GPSManager |
| - Provide a pool of candidates for selection | LotteryEngine |
| - Handle "declined" slots by providing replacement draws | LotteryEngine |

---

### **Class: LotteryEngine**
| Responsibility | Collaborators |
| :--- | :--- |
| - Randomly select participants from a Waiting List | WaitingList |
| - Handle the "Redraw" logic if someone declines | WaitingList, Notification |
| - Ensure selection fairness and capacity limits | Event |

---

## Infrastructure Classes

### **Class: FirebaseManager**
| Responsibility | Collaborators |
| :--- | :--- |
| - Synchronize event and user data in real-time | All Classes |
| - Handle authentication and role-based access | Entrant, Organizer, Admin |
| - Store and retrieve event poster images | ImageUploader |

---

### **Class: NotificationService**
| Responsibility | Collaborators |
| :--- | :--- |
| - Alert Entrants when they are "Sampled" | LotteryEngine, Entrant |
| - Alert Entrants if they were NOT selected | LotteryEngine |
| - Remind Entrants of upcoming deadlines | Event |

--- 

## Utility & Supporting Classes

### **Class: QRScanner**
| Responsibility | Collaborators |
| :--- | :--- |
| - Interface with the device camera to capture codes | *None* |
| - Decode QR data into a usable Event ID | Event |
| - Handle errors for invalid or corrupted codes | Entrant |

---

### **Class: ImageUploader**
| Responsibility | Collaborators |
| :--- | :--- |
| - Compress and format images for storage | *None* |
| - Upload event posters to Firebase Storage | FirebaseManager |
| - Retrieve image URLs for display in the app | Event, User |

---

### **Class: GPSManager**
| Responsibility | Collaborators |
| :--- | :--- |
| - Request and verify device location permissions | *None* |
| - Capture current Latitude/Longitude coordinates | EntrantLocation |
| - Compare user location against event requirements | Event |

---

### **Class: QRCodeGenerator**
| Responsibility | Collaborators |
| :--- | :--- |
| - Convert Event metadata into a unique QR code image | Event |
| - Save generated QR codes to the device or cloud | FirebaseManager |

---

### **Class: Profile**
| Responsibility | Collaborators |
| :--- | :--- |
| - Display user data (Name, Email, etc.) | User |
| - Provide interface for profile editing and deletion | FirebaseManager |
| - Display event participation history | NotificationList |

---

### **Class: DataExporter**
| Responsibility | Collaborators |
| :--- | :--- |
| - Compile Enrolled Entrant data into a CSV format | NotificationList |
| - Trigger the Android system "Share" or "Save" intent | *None* |