describe('Home page', () => {
    it('Validate the components of the Home page', () => {
      cy.visit('/home');
      cy.get(':nth-child(5) > .mdc-button__label').contains('Home')
      cy.get('h1').contains('Welcome')
      cy.get('.mat-mdc-nav-list > :nth-child(2)').should('exist')
      cy.get('.mat-mdc-nav-list > :nth-child(4)').should('exist')
      cy.get('.mat-mdc-nav-list > :nth-child(6)').should('exist')
    })
})
  