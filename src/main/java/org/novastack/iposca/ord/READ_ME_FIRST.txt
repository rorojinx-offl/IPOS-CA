IPOS-SA gave us an endpoint that performs browser handoff to their merchant portal, it is not an API contract for system-to-system integration.

For IPOS-CA ORD marks, we need programmatic exchange: IPOS-CA must call endpoints, receive structured data (JSON), render it in our UI, and send actions like order creation back to IPOS-SA without leaving our app.

A browser redirect cannot satisfy that because we lose control of data flow and cannot stream, transform, validate, or persist responses in IPOS-CA.

For the assessment, we therefore implemented the CA side as a proper integration client architecture (controller -> facade -> API client) and demonstrated the full ORD workflow with structured request/response handling.

If SA’s live API is unavailable or only supports portal handoff, we prove the same behavior through mock mode and contract-ready real-mode code, showing exactly where real JSON endpoints plug in once provided.

We isolated API response mapping in OrderSaFacade, so if IPOS-SA changes JSON field names or nesting, we only need small updates in the facade mapping logic. UI controllers and FXML pages stay mostly unchanged.

This demonstrates we completed the IPOS-CA responsibilities even when SA’s deliverable is incomplete.