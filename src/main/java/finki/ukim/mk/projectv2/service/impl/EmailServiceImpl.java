package finki.ukim.mk.projectv2.service.impl;

import com.sun.mail.imap.protocol.FLAGS;
import finki.ukim.mk.projectv2.model.Application;
import finki.ukim.mk.projectv2.model.OpenJobPosition;
import finki.ukim.mk.projectv2.model.Person;
import finki.ukim.mk.projectv2.model.Phase;
import finki.ukim.mk.projectv2.service.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;


@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;
    private final ApplicationService applicationService;
    private final OpenJobPositionService openJobPositionService;
    private final PersonService personService;
    private final PhaseService phaseService;

    public EmailServiceImpl(JavaMailSender emailSender, ApplicationServiceImpl applicationService, OpenJobPositionServiceImpl openJobPositionService, PersonServiceImpl personService, PhaseService phaseService) {
        this.emailSender = emailSender;
        this.applicationService = applicationService;
        this.openJobPositionService = openJobPositionService;
        this.personService = personService;
        this.phaseService = phaseService;
    }

    public void sendSimpleMessage(String to, String subject, String message){
        SimpleMailMessage message1 = new SimpleMailMessage();

        message1.setFrom("recruitment.process.project@gmail.com");
        message1.setTo(to);
        message1.setSubject(subject);
        message1.setText(message);

        emailSender.send(message1);

        System.out.println("Mail sent successfully");
    }

    @Override
    public void sendMessageWithAttachment(String to, String subject, String body, String fileToAttach) throws MessagingException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

        mimeMessageHelper.setFrom("recruitment.process.project@gmail.com");
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(body);

        FileSystemResource fileSystemResource = new FileSystemResource(new File(fileToAttach));

        mimeMessageHelper.addAttachment(fileSystemResource.getFilename(), fileSystemResource);

        emailSender.send(mimeMessage);

        System.out.println("Mail with attachment sent successfully");
    }
@Override
    public void sendTask(String to, String subject, String body, byte[] data) throws MessagingException, IOException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

        mimeMessageHelper.setFrom("recruitment.process.project@gmail.com");
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(body);

        Path path= Paths.get("src/main/java/finki/ukim/mk/projectv2/bootstrap/tempFile.txt"); //Create temp file
        Files.write(path,data);    //From database to temporary file

        FileSystemResource fileSystemResource = new FileSystemResource("src/main/java/finki/ukim/mk/projectv2/bootstrap/tempFile.txt");

        mimeMessageHelper.addAttachment(fileSystemResource.getFilename(), fileSystemResource);

        emailSender.send(mimeMessage);

        System.out.println("Random Task send");

        Files.delete(path);   //delete temp file
    }

    @Override
    public void check() {
        try {
            String host = "pop.gmail.com";// change accordingly
            String user = "recruitment.process.project@gmail.com";// change accordingly
            String password = "zqrnakwkklanbobb";// change accordingly

            //create properties field
            Properties properties = new Properties();

            properties.put("mail.pop3.host", host);
            properties.put("mail.pop3.port", "995");
            properties.put("mail.pop3.starttls.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);

            //create the POP3 store object and connect with the pop server
            Store store = emailSession.getStore("pop3s");

            store.connect(host, user, password);

            //create the folder object and open it
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            // retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.getMessages();
            System.out.println("messages.length---" + messages.length);

            for (int i = 0, n = messages.length; i < n; i++) {
                Message message = messages[i];
                if(message.isSet(Flags.Flag.DELETED) || message.getFlags().contains(Flags.Flag.DELETED)) continue;
                System.out.println("---------------------------------");
                System.out.println("Email Number " + (i + 1));
                System.out.println("Subject: " + message.getSubject());
                System.out.println("From: " + message.getFrom()[0]);
                System.out.println("Text: " + message.getContent().toString());

                Address sender = message.getFrom()[0];
                String[] parts = sender.toString().split("<");
                System.out.println(parts[0]);

                String[] nameSurname = parts[0].split(" ");

                String mail = parts[1].substring(0, parts[1].length()-1);
                System.out.println(mail);

                String subject = message.getSubject();
                System.out.println(subject);

                Phase phase = this.phaseService.findById(1L).get();

                Optional<Person> p = this.personService.saveWithPhaseNoAge(nameSurname[0], "", mail, phase);

                String ojpname;
                int pom = subject.lastIndexOf(']');
                if(pom != -1){
                     ojpname = subject.substring(1, pom);
                }
                else ojpname = "oopsie";

                Optional<OpenJobPosition> ojp = this.openJobPositionService.findByName(ojpname);
                if(ojp.isEmpty()){
                    sendSimpleMessage(mail, "Application Failed", "Dear " + parts[0] +
                            ", you have not formatted your email correctly. That is why your application has been declined." +
                            "" +
                            "" +
                            "Greetings," +
                            "Recruitment Process Team");
                }
                else{
                    p.ifPresent(person -> {
                        Application a=this.applicationService.save(person, ojp.get()).get();
                        sendSimpleMessage(mail, "Recruitment process(WP-project)", "Hello Mrs/Mr " +
                                "\n\nThank you for your application" +
                                "\n Your application ID(ticket) is " + a.getApplicationID() +
                                "\n\n Recruitment process team");

                    } );
                    sendSimpleMessage(mail, "Successful Application", "Successfully Applied !");

                }
                message.setFlag(Flags.Flag.DELETED,true);
                System.out.println(message.isSet(Flags.Flag.DELETED));
            }

            //close the store and folder objects
            emailFolder.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fetch() {
        try {
            // create properties field

            String host = "pop.gmail.com";// change accordingly
            String user = "recruitment.process.project@gmail.com";// change accordingly
            String password = "zqrnakwkklanbobb";// change accordingly

            Properties properties = new Properties();
            properties.put("mail.store.protocol", "pop3");
            properties.put("mail.pop3.host", host);
            properties.put("mail.pop3.port", "995");
            properties.put("mail.pop3.starttls.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);
            // emailSession.setDebug(true);

            // create the POP3 store object and connect with the pop server
            Store store = emailSession.getStore("pop3s");

            store.connect(host, user, password);

            // create the folder object and open it
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            // retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.getMessages();
            System.out.println("messages.length---" + messages.length);

            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                System.out.println("---------------------------------");
                writePart(message);
            }

            // close the store and folder objects
            emailFolder.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void writePart(Part p) throws Exception {
        if (p instanceof Message)
            //Call methos writeEnvelope
            writeEnvelope((Message) p);

        System.out.println("----------------------------");
        System.out.println("CONTENT-TYPE: " + p.getContentType());

        //check if the content is plain text
        if (p.isMimeType("text/plain")) {
            System.out.println("This is plain text");
            System.out.println("---------------------------");
            System.out.println((String) p.getContent());
        }
        //check if the content has attachment
        else if (p.isMimeType("multipart/*")) {
            System.out.println("This is a Multipart");
            System.out.println("---------------------------");
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++)
                writePart(mp.getBodyPart(i));
        }
        //check if the content is a nested message
        else if (p.isMimeType("message/rfc822")) {
            System.out.println("This is a Nested Message");
            System.out.println("---------------------------");
            writePart((Part) p.getContent());
        }
        //check if the content is an inline image
        else if (p.isMimeType("image/jpeg")) {
            System.out.println("--------> image/jpeg");
            Object o = p.getContent();

            InputStream x = (InputStream) o;
            // Construct the required byte array
            int i = 0;
            byte[] bArray = new byte[x.available()];
            System.out.println("x.length = " + x.available());
            while ((i = (int) ((InputStream) x).available()) > 0) {
                int result = (int) (((InputStream) x).read(bArray));
                if (result == -1)
                bArray = new byte[x.available()];
                break;
            }
            FileOutputStream f2 = new FileOutputStream("/tmp/image.jpg");
            f2.write(bArray);
        }
        else if (p.getContentType().contains("image/")) {
            System.out.println("content type" + p.getContentType());
            File f = new File("image" + new Date().getTime() + ".jpg");
            DataOutputStream output = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(f)));
            com.sun.mail.util.BASE64DecoderStream test =
                    (com.sun.mail.util.BASE64DecoderStream) p
                            .getContent();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = test.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
        else {
            Object o = p.getContent();
            if (o instanceof String) {
                System.out.println("This is a string");
                System.out.println("---------------------------");
                System.out.println((String) o);
            }
            else if (o instanceof InputStream) {
                System.out.println("This is just an input stream");
                System.out.println("---------------------------");
                InputStream is = (InputStream) o;
                is = (InputStream) o;
                int c;
                while ((c = is.read()) != -1)
                    System.out.write(c);
            }
            else {
                System.out.println("This is an unknown type");
                System.out.println("---------------------------");
                System.out.println(o.toString());
            }
        }

    }

    public static void writeEnvelope(Message m) throws Exception {
        System.out.println("This is the message envelope");
        System.out.println("---------------------------");
        Address[] a;

        // FROM
        if ((a = m.getFrom()) != null) {
            for (int j = 0; j < a.length; j++)
                System.out.println("FROM: " + a[j].toString());
        }

        // TO
        if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
            for (int j = 0; j < a.length; j++)
                System.out.println("TO: " + a[j].toString());
        }

        // SUBJECT
        if (m.getSubject() != null)
            System.out.println("SUBJECT: " + m.getSubject());

    }

}

