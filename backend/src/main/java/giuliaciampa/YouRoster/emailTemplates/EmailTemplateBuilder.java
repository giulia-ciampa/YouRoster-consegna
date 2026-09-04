package giuliaciampa.YouRoster.emailTemplates;

import java.time.LocalDate;
import java.time.LocalTime;

public class EmailTemplateBuilder {

    public static String buildResetPassword(String name, String surname, String link) {

        return
                """
                         <!DOCTYPE html>
                         <html lang="it">
                         <head>
                         <meta charset="UTF-8">
                         <meta name="viewport" content="width=device-width, initial-scale=1.0">
                         <title>Resetta password</title>
                         </head>
                         <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                        
                         <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                         <tr>
                         <td align="center">
                        
                         <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                        
                         <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                         <tr>
                         <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                         <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                         </td>
                         </tr>
                        
                        <!-- Body con testo Carbon black (#252218) e titolo (#E35226) -->
                        <tr>
                        <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                        <h2 style="margin-top: 0; color: #E35226; font-size: 20px;">Richiesta reset password</h2>
                        <p style="color: #252218;">Ciao <strong>%s</strong> <strong>%s</strong>,</p>
                        <p style="color: #252218;">Hai richiesto di reimpostare la password per il tuo account YouRoster. Clicca sul pulsante qui sotto per procedere (il link è valido per 15 minuti):</p>
                        
                        <!-- Pulsante Call to Action -->
                        <table border="0" cellpadding="0" cellspacing="0" style="margin: 25px 0;">
                        <tr>
                        <td align="center" bgcolor="#E35226" style="border-radius: 6px;">
                        <a href="%s" target="_blank" style="font-size: 16px; font-family: Arial, sans-serif; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 6px; border: 1px solid #E35226; display: inline-block; font-weight: bold;">Reimposta Password</a>
                        </td>
                        </tr>
                        </table>
                        
                        <p style="color: #888888; font-size: 13px;">Se non hai richiesto tu questo cambio, puoi ignorare tranquillamente questa email.</p>
                        </td>
                        </tr>
                        
                        
                         </table>
                         </td>
                         </tr>
                        
                         <!-- Footer -->
                         <tr>
                         <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                         <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                         </td>
                         </tr>
                        
                         </table>
                        
                         </td>
                         </tr>
                         </table>
                        
                         </body>
                         </html>
                        
                        """.formatted(name, surname, link);
    }

    public static String buildAccountApprovalEmail(String userName, String roleName, String officeName, String loginUrl) {
        String officeInfo = (officeName != null && !officeName.isBlank()) ? officeName : "Nessun ufficio specifico";
        return
                """
                        <!DOCTYPE html>
                         <html lang="it">
                         <head>
                         <meta charset="UTF-8">
                         <meta name="viewport" content="width=device-width, initial-scale=1.0">
                         <title>Account Attivato</title>
                         </head>
                         <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                        
                         <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                         <tr>
                         <td align="center">
                        
                         <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                        
                         <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                         <tr>
                         <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                         <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                         </td>
                         </tr>
                        
                         <!-- Body con testo Carbon black (#252218) e titolo (#E35226) -->
                         <tr>
                         <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                         <h2 style="margin-top: 0; color: #E35226; font-size: 20px;">Account Attivato! 🎉</h2>
                         <p style="color: #252218;">Ciao <strong>%s</strong>,</p>
                         <p style="color: #252218;">Il tuo account è stato approvato ed è pronto all'uso.</p>
                        
                         <div style="background-color: #f9f9f6; border-left: 4px solid #5D00E0; padding: 12px 16px; margin: 20px 0; border-radius: 0 6px 6px 0;">
                         <p style="margin: 4px 0; color: #252218;"><strong>Ruolo assegnato:</strong> <span style="color: #5D00E0; font-weight: bold;">%s</span></p>
                         <p style="margin: 4px 0; color: #252218;"><strong>Sede / Ufficio:</strong> <span style="color: #5D00E0;"> %s</span></p>
                         </div>
                         </td>
                         </tr>
                        
                         <!-- Bottone Login in Ultrasonic Blue (#5D00E0) -->
                         <tr>
                         <td align="center" style="padding: 25px 0 15px 0;">
                         <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                         <tr>
                         <td align="center" bgcolor="#5D00E0" style="border-radius: 6px;">
                         <a href="%s" target="_blank" style="font-size: 15px; font-weight: bold; color: #ffffff !important; text-decoration: none; padding: 14px 32px; display: inline-block; border-radius: 6px; background-color: #5D00E0;">LOGIN</a>
                         </td>
                         </tr>
                         </table>
                         </td>
                         </tr>
                        
                         <!-- Footer -->
                         <tr>
                         <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                         <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                         </td>
                         </tr>
                        
                         </table>
                        
                         </td>
                         </tr>
                         </table>
                        
                         </body>
                         </html>
                        """.formatted(userName, roleName, officeInfo, loginUrl);
    }

    public static String buildAccountDisableEmail(String userName) {
        return """
                        <!DOCTYPE html>
                         <html lang="it">
                         <head>
                         <meta charset="UTF-8">
                         <meta name="viewport" content="width=device-width, initial-scale=1.0">
                         <title>Account disabilitato</title>
                         </head>
                         <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                
                         <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                         <tr>
                         <td align="center">
                
                         <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                
                         <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                         <tr>
                         <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                         <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                         </td>
                         </tr>
                
                         <!-- Body con testo Carbon black (#252218) -->
                         <tr>
                         <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                         <h2 style="margin-top: 0; color: #E35226; font-size: 15px;">Account disabilitato.</h2>
                         <p style="color: #252218;">Ciao <strong>%s</strong>,</p>
                         <p style="color: #252218;">Il tuo account è stato disabilitato, grazie per aver lavorato con noi.</p>
                         </td>
                         </tr>
                
                
                         <!-- Footer -->
                         <tr>
                         <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                         <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                         </td>
                         </tr>
                
                         </table>
                
                         </td>
                         </tr>
                         </table>
                
                         </body>
                         </html>
                """.formatted(userName);
    }

    public static String buildAccountRejectionEmail(String email) {
        return
                """
                        <!DOCTYPE html>
                                 <html lang="it">
                                 <head>
                                 <meta charset="UTF-8">
                                 <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                 <title>Account rifiutato</title>
                                 </head>
                                 <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                        
                                 <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                                 <tr>
                                 <td align="center">
                        
                                 <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                        
                                 <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                                 <tr>
                                 <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                                 <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                                 </td>
                                 </tr>
                        
                                 <!-- Body con testo Carbon black (#252218) -->
                                 <tr>
                                 <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                                 <h2 style="margin-top: 0; color: #E35226; font-size: 15px;">Aggiornamento Dati Sensibili.</h2>
                                 <p style="color: #252218;">Gentile utente, la registrazione con l'email  <strong>%s</strong> non è stata accettata.</p>
                                 <p>Si prega di contattarci telefonicamente</p>
                                 </td>
                                 </tr>
                        
                        
                                 <!-- Footer -->
                                 <tr>
                                 <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                                 <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                                 </td>
                                 </tr>
                        
                                 </table>
                        
                                 </td>
                                 </tr>
                                 </table>
                        
                                 </body>
                                 </html>
                        """.formatted(email);
    }


    public static String buildUpdateSensitiveData(String employeeName, String details, String profileUpdated) {
        return
                """
                         <!DOCTYPE html>
                                         <html lang="it">
                                         <head>
                                         <meta charset="UTF-8">
                                         <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                         <title>Account aggiornato</title>
                                         </head>
                                         <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                        
                                         <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                                         <tr>
                                         <td align="center">
                        
                                         <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                        
                                         <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                                         <tr>
                                         <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                                         <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                                         </td>
                                         </tr>
                        
                                         <!-- Body con testo Carbon black (#252218) -->
                                         <tr>
                                         <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                                         <h2 style="margin-top: 0; color: #E35226; font-size: 15px;">Aggiornamento Dati Sensibili.</h2>
                                         <p style="color: #252218;">L'utente <strong>%s</strong> ha appena aggiornato <strong>%s</strong> nel proprio profilo.</p>
                                         <p>Clicca sul pulsante sottostante per verificare direttamente la scheda e i nuovi documenti inseriti:</p>
                        
                                         <!-- Pulsante Call to Action -->
                                                     <div style="text-align: center; margin: 30px 0 20px 0;">
                                                         <a href="%s" target="_blank" style="background-color: #5D00E0; color: #ffffff; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block; font-size: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                                             Visualizza Scheda Utente
                                                         </a>
                                                     </div>
                        
                                         </td>
                                         </tr>
                        
                        
                                         <!-- Footer -->
                                         <tr>
                                         <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                                         <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                                         </td>
                                         </tr>
                        
                                         </table>
                        
                                         </td>
                                         </tr>
                                         </table>
                        
                                         </body>
                                         </html>
                        """.formatted(employeeName, details, profileUpdated);
    }

    public static String buildUpdateRole(String employeeName, String roleName, String roleUpdate) {
        return
                """
                         <!DOCTYPE html>
                                         <html lang="it">
                                         <head>
                                         <meta charset="UTF-8">
                                         <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                         <title>Account aggiornato</title>
                                         </head>
                                         <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                        
                                         <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                                         <tr>
                                         <td align="center">
                        
                                         <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                        
                                         <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                                         <tr>
                                         <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                                         <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                                         </td>
                                         </tr>
                        
                                         <!-- Body con testo Carbon black (#252218) -->
                                         <tr>
                                         <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                                         <h2 style="margin-top: 0; color: #E35226; font-size: 15px;">Il tuo ruolo è stato modificato.</h2>
                                         <p style="color: #252218;">%s</p>
                                         <p style="color: #252218;">Ti è appena stato assegnato il ruolo di <strong>%s</strong>.</p>
                                         <p>Ora puoi vedere la tua posizione aggiornata nel tuo profilo, cliccando sul bottone sottostante</p>
                        
                                         <!-- Pulsante Call to Action -->
                                                     <div style="text-align: center; margin: 30px 0 20px 0;">
                                                         <a href="%s" target="_blank" style="background-color: #5D00E0; color: #ffffff; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block; font-size: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                                             Visualizza Scheda Utente
                                                         </a>
                                                     </div>
                        
                                         </td>
                                         </tr>
                        
                        
                                         <!-- Footer -->
                                         <tr>
                                         <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                                         <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                                         </td>
                                         </tr>
                        
                                         </table>
                        
                                         </td>
                                         </tr>
                                         </table>
                        
                                         </body>
                                         </html>
                        """.formatted(employeeName, roleName, roleUpdate);
    }

    public static String buildShiftUpdated(String officeName, LocalTime startShift, LocalTime endShift, LocalDate shiftDate, String employeeName, String userPageUrl) {
        return """
                
                <!DOCTYPE html>
                                         <html lang="it">
                                         <head>
                                         <meta charset="UTF-8">
                                         <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                         <title>Turno aggiornato</title>
                                         </head>
                                         <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                
                                         <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                                         <tr>
                                         <td align="center">
                
                                         <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                
                                         <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                                         <tr>
                                         <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                                         <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                                         </td>
                                         </tr>
                
                                         <!-- Body con testo Carbon black (#252218) -->
                                         <tr>
                                         <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                                         <h2 style="margin-top: 0; color: #E35226; font-size: 15px;">Il turno presso l'ufficio di <strong>%s</strong> con orario <strong>%s</strong> - <strong>%s</strong>, programmato in data <strong>%s</strong> è stato aggiornato.</h2>
                                         <p style="color: #252218;">Ciao %s</p>
                                         <p style="color: #252218;">Il tuo turno è stato aggiornato.</p>
                                         <p>Puoi vedere l'aggiornamento sul tuo profilo, cliccando sul bottone sottostante</p>
                
                                         <!-- Pulsante Call to Action -->
                                                     <div style="text-align: center; margin: 30px 0 20px 0;">
                                                         <a href="%s" target="_blank" style="background-color: #5D00E0; color: #ffffff; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block; font-size: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                                             Visualizza Scheda Utente
                                                         </a>
                                                     </div>
                
                                         </td>
                                         </tr>
                
                
                                         <!-- Footer -->
                                         <tr>
                                         <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                                         <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                                         </td>
                                         </tr>
                
                                         </table>
                
                                         </td>
                                         </tr>
                                         </table>
                
                                        </body>
                                         </html>
                
                """.formatted(officeName, startShift, endShift, shiftDate, employeeName, userPageUrl);
    }


    public static String buildNewTask(String officeName, LocalTime startShift, LocalTime endShift, LocalDate shiftDate, String employeeName, String taskTile, String taskPageUrl) {
        return """
                
                <!DOCTYPE html>
                                         <html lang="it">
                                         <head>
                                         <meta charset="UTF-8">
                                         <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                         <title>Mansione salvata</title>
                                         </head>
                                         <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                
                                         <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                                         <tr>
                                         <td align="center">
                
                                         <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                
                                         <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                                         <tr>
                                         <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                                         <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                                         </td>
                                         </tr>
                
                                         <!-- Body con testo Carbon black (#252218) -->
                                         <tr>
                                         <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                                         <h2 style="margin-top: 0; color: #E35226; font-size: 15px;">C'è stato un aggiornamento delle mansioni per il  turno presso <strong>%s</strong>, con orario <strong>%s</strong> - <strong>%s</strong> programmato in data <strong>%s</strong>.</h2>
                                         <p style="color: #252218;">Ciao %s</p>
                                         <p style="color: #252218;">la mansione <strong>%s</strong> è stata aggiornata.</p>
                                         <p>Puoi vedere gli aggiornamenti sul tuo profilo, cliccando sul bottone sottostante</p>
                
                                         <!-- Pulsante Call to Action -->
                                                     <div style="text-align: center; margin: 30px 0 20px 0;">
                                                         <a href="%s" target="_blank" style="background-color: #5D00E0; color: #ffffff; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block; font-size: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                                             Visualizza Scheda Utente
                                                         </a>
                                                     </div>
                
                                         </td>
                                         </tr>
                
                
                                         <!-- Footer -->
                                         <tr>
                                         <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                                         <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                                         </td>
                                         </tr>
                
                                         </table>
                
                                         </td>
                                         </tr>
                                         </table>
                
                                        </body>
                                         </html>
                
                """.formatted(officeName, startShift, endShift, shiftDate, employeeName, taskTile, taskPageUrl);
    }

    public static String buildCancelledHolidayRequest(String singleRole, String userName, String userSurname, LocalDate startDate, LocalDate endDate) {
        return
                """
                        
                        <!DOCTYPE html>
                                                 <html lang="it">
                                                 <head>
                                                 <meta charset="UTF-8">
                                                 <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                 <title>Richiesta annullata</title>
                                                 </head>
                                                 <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                        
                                                 <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                                                 <tr>
                                                 <td align="center">
                        
                                                 <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                        
                                                 <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                                                 <tr>
                                                 <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                                                 <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                                                 </td>
                                                 </tr>
                        
                                                 <!-- Body con testo Carbon black (#252218) -->
                                                 <tr>
                                                 <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                                                 <h2 style="margin-top: 0; color: #E35226; font-size: 15px;">Una richiesta è appena stata cancellata.</h2>
                                                 <p style="color: #252218;">Gentile %s,</p>
                                                 <p style="color: #252218;">L'utente <strong>%s</strong> <strong>%s</strong> ha appena annullato la sua recente richiesta per le date <strong>%s</strong> - <strong>%s</strong> precedentemente approvata.</p>
                                                 <p>È quindi di nuovo disponibile per l'assegnazione dei turni</p>.
                        
                                                 </td>
                                                 </tr>
                        
                                                 <!-- Footer -->
                                                 <tr>
                                                 <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                                                 <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                                                 </td>
                                                 </tr>
                        
                                                 </table>
                        
                                                 </td>
                                                 </tr>
                                                 </table>
                        
                                                </body>
                                                 </html>
                        
                        """
                        .formatted(singleRole, userName, userSurname, startDate, endDate);
    }

    public static String builCancelledLeaveHoursRequest(String singleRole, String userName, String userSurname, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return
                """
                        
                        <!DOCTYPE html>
                                                 <html lang="it">
                                                 <head>
                                                 <meta charset="UTF-8">
                                                 <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                 <title>Richiesta annullata</title>
                                                 </head>
                                                 <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                        
                                                 <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                                                 <tr>
                                                 <td align="center">
                        
                                                 <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                        
                                                 <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                                                 <tr>
                                                 <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                                                 <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                                                 </td>
                                                 </tr>
                        
                                                 <!-- Body con testo Carbon black (#252218) -->
                                                 <tr>
                                                 <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                                                 <h2 style="margin-top: 0; color: #E35226; font-size: 15px;">Una richiesta è appena stata cancellata.</h2>
                                                 <p style="color: #252218;">Gentile %s,</p>
                                                 <p style="color: #252218;">L'utente <strong>%s</strong> <strong>%s</strong> ha appena annullato la sua recente richiesta per le data <strong>%s</strong> con orario <strong>%s</strong> - <strong>%s</strong> precedentemente approvata.</p>
                                                 <p>È quindi di nuovo disponibile per l'assegnazione dei turni</p>.
                        
                                                 </td>
                                                 </tr>
                        
                                                 <!-- Footer -->
                                                 <tr>
                                                 <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                                                 <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                                                 </td>
                                                 </tr>
                        
                                                 </table>
                        
                                                 </td>
                                                 </tr>
                                                 </table>
                        
                                                </body>
                                                 </html>
                        
                        """.formatted(singleRole, userName, userSurname, date, startTime, endTime);

    }

    public static String buildHolidayRequest(String singleRole, String userName, String userSurname, LocalDate startDate, LocalDate endDate, String newRequestUrl) {
        return """
                <!DOCTYPE html>
                                         <html lang="it">
                                         <head>
                                         <meta charset="UTF-8">
                                         <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                         <title>Nuova richiesta</title>
                                         </head>
                                         <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                
                                         <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                                         <tr>
                                         <td align="center">
                
                                         <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                                         <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                
                                         <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                                         <tr>
                                         <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                                         <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                                         </td>
                                         </tr>
                
                                         <!-- Body con testo Carbon black (#252218) -->
                                         <tr>
                                         <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                                         <h2 style="margin-top: 0; color: #E35226; font-size: 15px;">Nuova richiesta di ferie.</h2>
                                         <p style="color: #252218;">Gentile %s,</p>
                                         <p style="color: #252218;">L'utente <strong>%s</strong> <strong>%s</strong> ha appena inviato una nuova richiesta di ferie per le date <strong>%s</strong> - <strong>%s</strong>.</p>
                
                
                                         <!-- Pulsante Call to Action -->
                                             <div style="text-align: center; margin: 30px 0 20px 0;">
                                                 <a href="%s" target="_blank" style="background-color: #5D00E0; color: #ffffff; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block; font-size: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                                     Visualizza richiesta
                                                 </a>
                                             </div>
                
                                         </td>
                                         </tr>
                
                                         <!-- Footer -->
                                         <tr>
                                         <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                                         <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                                         </td>
                                         </tr>
                
                                         </table>
                
                                         </td>
                                         </tr>
                                         </table>
                
                                        </body>
                                         </html>
                
                """

                .formatted(singleRole, userName, userSurname, startDate, endDate, newRequestUrl);
    }


    public static String buildResponse(String userName, String urlUserDashboard) {
        return
                """
                        <!DOCTYPE html>
                                                 <html lang="it">
                                                 <head>
                                                 <meta charset="UTF-8">
                                                 <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                 <title>Risposta alla richiesta</title>
                                                 </head>
                                                 <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                        
                                                 <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                                                 <tr>
                                                 <td align="center">
                        
                                                 <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                        
                                                 <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                                                 <tr>
                                                 <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                                                 <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                                                 </td>
                                                 </tr>
                        
                                                 <!-- Body con testo Carbon black (#252218) -->
                                                 <tr>
                                                 <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                                                 <h2 style="margin-top: 0; color: #E35226; font-size: 15px;">La tua richiesta è stata valutata.</h2>
                                                 <p style="color: #252218;">Gentile %s,</p>
                                                 <p style="color: #252218;">La tua richiesta è stata valutata. Puoi verificare l'esito e lo stato aggiornato accedendo direttamente alla tua dashboard.</p>
                        
                                                 <!-- Pulsante Call to Action -->
                                                     <div style="text-align: center; margin: 30px 0 20px 0;">
                                                         <a href="%s" target="_blank" style="background-color: #5D00E0; color: #ffffff; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block; font-size: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                                             Vai alla dashboard
                                                         </a>
                                                     </div>
                        
                                                 </td>
                                                 </tr>
                        
                                                 <!-- Footer -->
                                                 <tr>
                                                 <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                                                 <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                                                 </td>
                                                 </tr>
                        
                                                 </table>
                        
                                                 </td>
                                                 </tr>
                                                 </table>
                        
                                                </body>
                                                 </html>
                        
                        """.formatted(userName, urlUserDashboard);
    }

    public static String buildLeaveRequest(String singleRole, String userName, String userSurname, LocalDate date, LocalTime startTime, LocalTime endTime, String newRequestUrl) {
        return
                """
                        
                        <!DOCTYPE html>
                                                 <html lang="it">
                                                 <head>
                                                 <meta charset="UTF-8">
                                                 <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                 <title>Nuova richiesta</title>
                                                 </head>
                                                 <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                        
                                                 <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                                                 <tr>
                                                 <td align="center">
                        
                                                 <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                        
                                                 <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                                                 <tr>
                                                 <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                                                 <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                                                 </td>
                                                 </tr>
                        
                                                 <!-- Body con testo Carbon black (#252218) -->
                                                 <tr>
                                                 <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                                                 <h2 style="margin-top: 0; color: #E35226; font-size: 15px;">Nuova richiesta ore di permesso.</h2>
                                                 <p style="color: #252218;">Gentile %s,</p>
                                                 <p style="color: #252218;">L'utente <strong>%s</strong> <strong>%s</strong> ha appena inviato una nuova richiesta di permesso per la data <strong>%s</strong> dalle ore <strong>%s</strong> alle ore <strong>%s</strong>.</p>
                        
                        
                                             <!-- Pulsante Call to Action -->
                                             <div style="text-align: center; margin: 30px 0 20px 0;">
                                                 <a href="%s" target="_blank" style="background-color: #5D00E0; color: #ffffff; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block; font-size: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                                     Visualizza richiesta
                                                 </a>
                                             </div>
                        
                                                 </td>
                                                 </tr>
                        
                                                 <!-- Footer -->
                                                 <tr>
                                                 <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                                                 <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                                                 </td>
                                                 </tr>
                        
                                                 </table>
                        
                                                 </td>
                                                 </tr>
                                                 </table>
                        
                                                </body>
                                                 </html>
                        
                        """

                        .formatted(singleRole, userName, userSurname, date, startTime, endTime, newRequestUrl);
    }

    public static String buildChangeHolidayRequest(String singleRole, String userName, String userSurname, LocalDate startDate, LocalDate endDate, String dashboardUrl) {
        return """
                <!DOCTYPE html>
                                                 <html lang="it">
                                                 <head>
                                                 <meta charset="UTF-8">
                                                 <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                 <title>Nuova richiesta di modifica</title>
                                                 </head>
                                                 <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                
                                                 <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                                                 <tr>
                                                 <td align="center">
                
                                                 <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                
                                                 <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                                                 <tr>
                                                 <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                                                 <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                                                 </td>
                                                 </tr>
                
                                                 <!-- Body con testo Carbon black (#252218) -->
                                                 <tr>
                                                 <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                                                 <h2 style="margin-top: 0; color: #E35226; font-size: 15px;">Nuova richiesta di modifica.</h2>
                                                 <p style="color: #252218;">Gentile %s,</p>
                                                 <p style="color: #252218;">L'utente <strong>%s</strong> <strong>%s</strong> ha appena inviato una richiesta di modifica per il periodo <strong>%s</strong> - <strong>%s</strong>.</p>
                                                 <p style="color: #252218;">Puoi visionare i dettagli direttamente nella dashboard</p>
                
                
                                             <!-- Pulsante Call to Action -->
                                             <div style="text-align: center; margin: 30px 0 20px 0;">
                                                 <a href="%s" target="_blank" style="background-color: #5D00E0; color: #ffffff; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block; font-size: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                                     Visualizza richiesta
                                                 </a>
                                             </div>
                
                                                 </td>
                                                 </tr>
                
                                                 <!-- Footer -->
                                                 <tr>
                                                 <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                                                 <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                                                 </td>
                                                 </tr>
                
                                                 </table>
                
                                                 </td>
                                                 </tr>
                                                 </table>
                
                                                </body>
                                                 </html>
                """.formatted(singleRole, userName, userSurname, startDate, endDate, dashboardUrl);
    }

    public static String buildChangeLeaveHoursRequest(String singleRole, String userName, String userSurname, LocalDate date, LocalTime startTime, LocalTime endTime, String dashboardUrl) {


        return """
                
                <!DOCTYPE html>
                                                 <html lang="it">
                                                 <head>
                                                 <meta charset="UTF-8">
                                                 <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                 <title>Nuova richiesta di modifica</title>
                                                 </head>
                                                 <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                
                                                 <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                                                 <tr>
                                                 <td align="center">
                
                                                 <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                
                                                 <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                                                 <tr>
                                                 <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                                                 <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                                                 </td>
                                                 </tr>
                
                                                 <!-- Body con testo Carbon black (#252218) -->
                                                 <tr>
                                                 <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                                                 <h2 style="margin-top: 0; color: #E35226; font-size: 15px;">Nuova richiesta di modifica.</h2>
                                                 <p style="color: #252218;">Gentile %s,</p>
                                                 <p style="color: #252218;">L'utente <strong>%s</strong> <strong>%s</strong> ha appena inviato una richiesta di modifica per la data <strong>%s</strong>, dalle ore <strong>%s</strong> alle ore <strong>%s</strong>.</p>
                                                 <p style="color: #252218;">Puoi visionare i dettagli direttamente nella dashboard</p>
                
                
                                             <!-- Pulsante Call to Action -->
                                             <div style="text-align: center; margin: 30px 0 20px 0;">
                                                 <a href="%s" target="_blank" style="background-color: #5D00E0; color: #ffffff; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block; font-size: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                                     Visualizza richiesta
                                                 </a>
                                             </div>
                
                                                 </td>
                                                 </tr>
                
                                                 <!-- Footer -->
                                                 <tr>
                                                 <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                                                 <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                                                 </td>
                                                 </tr>
                
                                                 </table>
                
                                                 </td>
                                                 </tr>
                                                 </table>
                
                                                </body>
                                                 </html>
                
                """.formatted(singleRole, userName, userSurname, date, startTime, endTime, dashboardUrl);
    }

    public static String buildApproveCertificationRequest(String singleRole, String userName, String userSurname, LocalDate startDate, LocalDate endDate, String certificateType) {
        return """
                
                 <!DOCTYPE html>
                                                 <html lang="it">
                                                 <head>
                                                 <meta charset="UTF-8">
                                                 <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                 <title>Richiesta approvata</title>
                                                 </head>
                                                 <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                
                                                 <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                                                 <tr>
                                                 <td align="center">
                
                                                 <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                
                                                 <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                                                 <tr>
                                                 <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                                                 <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                                                 </td>
                                                 </tr>
                
                                                 <!-- Body con testo Carbon black (#252218) -->
                                                 <tr>
                                                 <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                                                 <h2 style="margin-top: 0; color: #E35226; font-size: 15px;">Il reparto Risorse Umane ha approvato un nuova richiesta di malattia.</h2>
                                                 <p style="color: #252218;">Gentile %s,</p>
                                                 <p style="color: #252218;">L'utente <strong>%s</strong> <strong>%s</strong> sarà assente dal lavoro dal giorno <strong>%s</strong> al giorno <strong>%s</strong> per <strong>%s</strong>.</p>
                
                
                                                 </td>
                                                 </tr>
                
                                                 <!-- Footer -->
                                                 <tr>
                                                 <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                                                 <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                                                 </td>
                                                 </tr>
                
                                                 </table>
                
                                                 </td>
                                                 </tr>
                                                 </table>
                
                                                </body>
                                                 </html>
                
                """.formatted(singleRole, userName, userSurname, startDate, endDate, certificateType);
    }

    public static String buildNewCertificationRequest(String singleRole, String userName, String userSurname, String certificateType, LocalDate startDate, LocalDate endDate, String link) {
        return """
                <!DOCTYPE html>
                                                 <html lang="it">
                                                 <head>
                                                 <meta charset="UTF-8">
                                                 <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                 <title>Nuova richiesta certificata</title>
                                                 </head>
                                                 <body style="margin: 0; padding: 0; background-color: #EAEAD7; font-family: Arial, sans-serif; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%;">
                
                                                 <!-- Layout Container Responsive: Sfondo Beige (#EAEAD7) -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #EAEAD7; padding: 30px 0;">
                                                 <tr>
                                                 <td align="center">
                
                                                 <!-- Card Principale su sfondo bianco con bordo #9E4289 -->
                                                 <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; border: 2px solid #9E4289; overflow: hidden; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                
                                                 <!-- Header con brand Ultrasonic Blue (#5D00E0) e bordo Grape Soda -->
                                                 <tr>
                                                 <td align="center" style="border-bottom: 2px solid #9E4289; padding-bottom: 15px;">
                                                 <h1 style="color: #E35226; margin: 0; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;"><span style="color: #9E4289;">You</span>Roster</h1>
                                                 </td>
                                                 </tr>
                
                                                 <!-- Body con testo Carbon black (#252218) -->
                                                 <tr>
                                                 <td style="padding-top: 25px; color: #33007A; font-size: 15px; line-height: 1.6;">
                                                 <h2 style="margin-top: 0; color: #E35226; font-size: 15px;">Nuova richiesta certificata.</h2>
                                                 <p style="color: #252218;">Gentile %s,</p>
                                                 <p style="color: #252218;">L'utente <strong>%s</strong> <strong>%s</strong> ha inviato una nuova richiesta per <strong>%s</strong> dal giorno <strong>%s</strong> al giorno <strong>%s</strong>.</p>
                
                
                                             <!-- Pulsante Call to Action -->
                                             <div style="text-align: center; margin: 30px 0 20px 0;">
                                                 <a href="%s" target="_blank" style="background-color: #5D00E0; color: #ffffff; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block; font-size: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                                     Visualizza richiesta
                                                 </a>
                                             </div>
                
                
                                                 </td>
                                                 </tr>
                
                                                 <!-- Footer -->
                                                 <tr>
                                                 <td align="center" style="border-top: 1px solid #E3E3CA; padding-top: 15px; font-size: 12px; color: #888888;">
                                                 <p style="margin: 0;">© YouRoster - Gestione Personale e Turni.</p>
                                                 </td>
                                                 </tr>
                
                                                 </table>
                
                                                 </td>
                                                 </tr>
                                                 </table>
                
                                                </body>
                                                 </html>
                """.formatted(singleRole, userName, userSurname, certificateType, startDate, endDate, link);


    }


}
      
