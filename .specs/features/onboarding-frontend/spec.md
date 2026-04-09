# Feature: Onboarding Frontend

## Goal
A single-page React app that lets a user fill in personal data and capture a webcam photo, then submit to `POST /onboarding` on `baas-onboarding`.

## Requirements

### Backend
| ID | Requirement |
|---|---|
| B-01 | `baas-onboarding` must accept cross-origin requests from `http://localhost:5173` |
| B-02 | CORS must allow `POST`, `GET`, `PATCH`, `OPTIONS` methods and `Content-Type`, `Accept` headers |

### Frontend — Form fields
| ID | Requirement |
|---|---|
| F-01 | Field: Full Name (`name`) — text, required |
| F-02 | Field: Email (`email`) — email, required |
| F-03 | Field: Phone (`phone`) — text, required |
| F-04 | Field: Document / CPF (`document`) — text, required |
| F-05 | Field: Birth Date (`birth_date`) — date, required |
| F-06 | Field: Mother's Name (`mother_name`) — text, required |
| F-07 | Address: Street (`address.street`) — text, required |
| F-08 | Address: City (`address.city`) — text, required |
| F-09 | Address: State (`address.state`) — text, required |
| F-10 | Address: ZIP (`address.zip`) — text, required |

### Frontend — Webcam
| ID | Requirement |
|---|---|
| W-01 | A "Take Photo" section shows the live webcam feed |
| W-02 | User clicks "Capture" to freeze the frame into a preview |
| W-03 | User can click "Retake" to go back to live feed |
| W-04 | Captured photo is base64-encoded and sent as `fingerprint` in the request body |
| W-05 | Submit is disabled until a photo has been captured |

### Frontend — Submission & Feedback
| ID | Requirement |
|---|---|
| S-01 | On success, show the returned `onboarding_id` with a success message |
| S-02 | On error, show a human-readable error message |
| S-03 | Submit button shows loading state while request is in flight |
| S-04 | All required fields are validated before submission |

## Out of Scope
- Status polling page
- Authentication
- Production CORS (any origin)
